/*
* 
* Copyright (C) 2015 Orange Labs
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*    http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
*/

package fr.ardeconnect.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import fr.ardeconnect.proxy.IRemoteListener;

import fr.ardeconnect.proxy.IRemoteService;
import fr.ardeconnect.proxy.IRemoteServiceInternal;

import fr.ardeconnect.proxy.R;

import fr.ardeconnect.proxy.WebViewActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


/**
 * Openid Connect proxy service class
 *
 */
public class Service extends android.app.Service {

	protected static final String TAG = Service.class.getName();
	
	final static String EMPTY = "";
	
	static {
	    Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}

	int idList=0;
	class RemoteListener {
		IRemoteListener listener;
		String id;
		String spEnrollUrl;
		RemoteListener(IRemoteListener r) {
			listener = r;
			idList++;
			id = ""+idList;
		}
	};
	
	List <RemoteListener> RemoteListenerList = new ArrayList<RemoteListener>();
	
	public static Service theService = null;
	
	public Service() {
		// android.os.Debug.waitForDebugger();
		System.setProperty("http.keepAlive", "false");
		if( theService == null ) {
			theService = this;
		}
		
	}

	// private final IRemoteServiceBinder mBinder = new IRemoteServiceBinder();

	public class ServiceBinder extends Binder {
		Service getService() {
			return Service.this;
		}
	}

	private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
		
		@Override
		public boolean enrollWithArdeco(
		        IRemoteListener listener,
		        String spEnrollUrl )
				throws RemoteException {

			// check access
			if( !checkCallingSignature() ) return false;

			// check parameters
			if( listener==null || isEmpty(spEnrollUrl) ) {
				// invalid parameters
				return false;
			}
			
			// android.os.Debug.waitForDebugger();

			Logd(TAG,"enrollWithArdeco begin");

			showNotProtectedNotifIcon();
			
	        RemoteListener rl;
			synchronized(RemoteListenerList) {
				rl = new RemoteListener(listener);
				rl.spEnrollUrl = spEnrollUrl;
				RemoteListenerList.add(rl);
			}

			// launch request
			// launch webview

			// init intent
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(theService, WebViewActivity.class);

			// prepare request parameters
			intent.putExtra("id", rl.id);

			intent.putExtra("spEnrollUrl", spEnrollUrl);
			intent.setFlags(
					  Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);

			// display webview
			theService.startActivity(intent);

			Logd(TAG,"enrollWithArdeco end");
			
			return true;

		}

		private boolean FORCE_CHECK = true;

		// check calling process signature, if not valid return false
		// possibility of hack
		private boolean checkCallingSignature() {
			if (FORCE_CHECK)
				return true;

			// get package name
			String pName = getPackageManager().getNameForUid(Binder.getCallingUid());
			X509Certificate certCalling = PackInfo.getCertificate(Service.this, pName);
			X509Certificate certProxy   = PackInfo.getCertificate(Service.this, Service.this.getPackageName());

			Logd(TAG, "certCalling : "+pName);
			Logd(TAG, ""+certCalling.toString());
			Logd(TAG, "certProxy : "+Service.this.getPackageName());
			Logd(TAG, ""+certProxy.toString());
			
			//certCalling.
			try {
				certCalling.verify(RsaKeyRootCA.pubRsaKey);
			} catch ( Exception e) {
				Logd(TAG,"checkCallingSignature failed");
				return false;
			}
			
			// uid are not the same
			Logd(TAG,"checkCallingSignature success");
			return true;
		}
	};

	void resetCookies() {
    	// init intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(Service.this, WebViewActivity.class);

        intent.putExtra("resetcookies", "true" );
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        // launch webview
        startActivity(intent);
	}
	
	long getSecretPathThreshold() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		long l= sharedPrefs.getLong("threshold", 80);
		return l;
	}
	
	void setSecretPathThreshold(long val) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPrefs.edit();
		editor.putLong("threshold", val);
		editor.commit();
		Logd(TAG,"threshold: "+val);
	}
	
	// internal remote service for internal webview
	private final IRemoteServiceInternal.Stub mBinderInternal = new IRemoteServiceInternal.Stub() {

		@Override
		public void cancel(String id, boolean user) throws RemoteException {
			
			// check caller uid
			if( checkCallingUid() == false ) {
				// caller are not from inside this app
				Logd(TAG,"setTokens not from inside this app");
				return;
			}

			RemoteListener rl=null;
			synchronized(RemoteListenerList) {
				for(int i=RemoteListenerList.size()-1; i>=0; i--) {
					RemoteListener r = RemoteListenerList.get(i);
					if(r.id.compareTo(id)==0) {
						rl = r;
						RemoteListenerList.remove(i);
						break;
					}
				}
			}
			
			if( rl != null  ) {
        		rl.listener.handleSpCode(EMPTY, EMPTY, user );
			}

			// hideNotifIcon();
		}

		@Override
		public void setSpCode( String id, String spCode ) {
			
			// check caller uid
			if( checkCallingUid() == false ) {
				// caller are not from inside this app
				Logd(TAG,"doRedirect not from inside this app");
				return;
			}

			Logd(TAG,"getSpCode begin");
			
			if( isEmpty(id) ) {
				Logd(TAG,"getSpCode end no ID");
				// hideNotifIcon();
				return;
			}

			RemoteListener rl = null;
			synchronized(RemoteListenerList) {
				for(int i=RemoteListenerList.size()-1; i>=0; i--) {
					RemoteListener r = RemoteListenerList.get(i);
					if(r.id.compareTo(id)==0) {
						RemoteListenerList.remove(i);
						rl = r;
						break;
					}
				}
			}

			if( rl != null ) {
				// get code from SP
        		try {
   					rl.listener.handleSpCode(spCode, EMPTY, false );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
	        Logd(TAG,"getSpCode end");
			hideNotifIcon();
		}

		@Override
		public void resetCookies() throws RemoteException {
			
			// check caller uid
			if( checkCallingUid() == false ) {
				// caller are not from inside this app
				Logd(TAG,"resetCookies not from inside this app");
				return;
			}

			// clear cookies
	        // android.webkit.CookieManager.getInstance().removeAllCookie();
		}

		// check calling process uid for possibility of hack,
		// if different from current service uid then return false
		private boolean checkCallingUid() {
			// check uid
			if( android.os.Process.myUid() == Binder.getCallingUid() ) {
				return true;
			}
			
			// uid are not the same
			return false;
		}

	};

	@Override
	public void onCreate() {
		super.onCreate();
	}

	// service termination
	@Override
	public void onDestroy() {
		hideNotifIcon();
		super.onDestroy();
	}
	
	// service starting
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
	}

	// new connection to service
    @Override
    public IBinder onBind(Intent intent) {
        // Select the interface to return.  If your service only implements
        // a single interface, you can just return it here without checking
        // the Intent.
        if (IRemoteService.class.getName().equals(intent.getAction())) {
    		showProtectedNotifIcon();
    		Logd(TAG,"onBind Service");
            return mBinder;
        }
        if (IRemoteServiceInternal.class.getName().equals(intent.getAction())) {
    		Logd(TAG,"onBind ServiceInternal");
            return mBinderInternal;
        }
        return null;
    }

	// get a string from a json object
	String getFromJS(JSONObject jo, String name){
		Log.d(TAG, "getFromJS " + name );
		if ( jo != null ) {
			try {
				return jo.getString(name);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	
	void toast(final String msg, final int duration) {
		new android.os.Handler(android.os.Looper.getMainLooper())
				.post(new Runnable() {
					@Override
					public void run() {
						android.widget.Toast
								.makeText(
										theService,
										msg,
										duration == 0 ? android.widget.Toast.LENGTH_SHORT
												: android.widget.Toast.LENGTH_LONG)
								.show();
					}
				});
	}
	
	
	private void showProtectedNotifIcon() {
		showNotification(true);
	}

	private void showNotProtectedNotifIcon() {
		showNotification(false);
	}
	
	private void showNotification(boolean bProtect){
		Logd(TAG,"show protected icon "+bProtect);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = null;
        
        if(bProtect) {
        	mNotification = new Notification.Builder(this)

            .setContentTitle("ARDECO PROXY")
            .setContentText("idle")
            .setSmallIcon(R.drawable.ardeco_off)
            .setAutoCancel(false)
            .build();
        } else {
        	mNotification = new Notification.Builder(this)

            .setContentTitle("ARDECO PROXY")
            .setContentText("active")
            .setSmallIcon(R.drawable.ardeco_on)
            .setAutoCancel(false)
            .build();
        }

        // to make it non clearable
        mNotification.flags |= Notification.FLAG_NO_CLEAR;
        
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below
        // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mNotification);
    }

    private void hideNotifIcon() {
		Logd(TAG,"hideNotifIcon");

        if (Context.NOTIFICATION_SERVICE!=null) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
            nMgr.cancel(0);
        }
    }

    void Logd(String tag, String msg) {
		if(tag!=null && msg!=null) Log.d(tag, msg);
	}

	// RSA key used by the Secure Proxy
	// here for demo only, must be stored in a secure element
	final static class RsaKeyRootCA {

		// RSA 512
		// static private String _rsaNs = "174534779388221555027193756296996728577785706931882582500865462191276991828908964783626120005370658240337053195460241456112314652761908126414596521509072923990652465127860275450913335454113631551447701718552251290927858625416498836268258006496356478412330739471783248956660708147504866504092613087913865419941";
        static private String _rsaNs = "137615577516537198807817310937859988960161111436453997102401245051154742289581142838915071683987098078139191437791365924492551201544861234198101701401783768985281927492472872690006772264611369208880786262064142563461367635639697166679608227153800046014276971655300012833498776331150965310640667300049423086361";
		static private String _rsaEs = "65537";

		static public  PublicKey pubRsaKey;
	}

	// init the key from the big numbers above
	static {
		BigInteger rsaN = null;
		BigInteger rsaE = null;
		try {
			rsaN = new BigInteger(RsaKeyRootCA._rsaNs);
			rsaE = new BigInteger(RsaKeyRootCA._rsaEs);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		
		RSAPublicKeySpec pubRsaSpec = new RSAPublicKeySpec(rsaN, rsaE);
		RsaKeyRootCA.pubRsaKey = null;

		try {
			KeyFactory keyfact = KeyFactory.getInstance("RSA","SC");
			RsaKeyRootCA.pubRsaKey = keyfact.generatePublic(pubRsaSpec);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// get a HTTP connector
	static public HttpURLConnection getHUC(String address) {
		HttpURLConnection http = null;
		try {
			URL url = new URL(address);

			if (url.getProtocol().equalsIgnoreCase("https")) {
				// only use trustAllHosts and DO_NOT_VERIFY in development
				// process
				trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection) url
						.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				http = https;
			} else {
				http = (HttpURLConnection) url.openConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return http;
	}

	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * WARNING : only use in development environment,
	 * DO NOT USE in production or commercial environments !!!
	 * Trust every server - do not check for any certificate
	 */
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] x509Certificates,
					String s) throws CertificateException {

			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] x509Certificates,
					String s) throws CertificateException {

			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	// return true is string is null or empty, false otherwise
	private static boolean isEmpty(String s) {
		if (s == null || s.length() == 0)
			return true;
		return false;
	}
}
