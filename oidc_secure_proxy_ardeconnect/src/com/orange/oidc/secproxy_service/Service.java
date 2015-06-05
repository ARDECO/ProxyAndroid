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

package com.orange.oidc.secproxy_service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.orange.oidc.secproxy_service.IRemoteListenerToken;
import com.orange.oidc.secproxy_service.IRemoteService;
import com.orange.oidc.secproxy_service.IRemoteServiceInternal;
import com.orange.oidc.secproxy_service.R;
import com.orange.oidc.secproxy_service.MySecureProxy.RsaKeyProxy;

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

	protected static final String TAG = "Service";
	
	final static String EMPTY = "";
	
	static {
	    Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}

	int idList=0;
	class RemoteListenerToken {
		IRemoteListenerToken listener;
		String id;
		OpenidConnectParams ocp;
		RemoteListenerToken(IRemoteListenerToken r) {
			listener = r;
			idList++;
			id = ""+idList;
		}
	};
	
	List <RemoteListenerToken> RemoteListenerTokenList = new ArrayList<RemoteListenerToken>();
	
	public static Service theService = null;
	
	private static SecureProxy secureProxy;
	
	public Service() {
		// android.os.Debug.waitForDebugger();
		System.setProperty("http.keepAlive", "false");
		if( theService == null ) {
			theService = this;
			// init secure storage
			secureProxy = new MySecureProxy();
			
			// init Http
			HttpOpenidConnect.secureProxy = secureProxy;
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
		public boolean getTokensWithOidcProxy(
		        IRemoteListenerToken listener,
		        String serverUrl,
		        String client_id,
				String scope,
				String state,
		        String nonce )
				throws RemoteException {

			// check access
			if( !checkCallingSignature() ) return false;

			// check parameters
			if( 	isEmpty(serverUrl)
				||	isEmpty(client_id)
				||	isEmpty(scope)
				) {
					// invalid parameters
					return false;
				}
			
			// android.os.Debug.waitForDebugger();

			Logd(TAG,"getTokensWithOidcProxy begin");

			showNotProtectedNotifIcon();
			
			if(!serverUrl.endsWith("/")) serverUrl += "/";
			
			scope = sortScope(scope+" secure_proxy");
			
	        OpenidConnectParams ocp = new OpenidConnectParams(serverUrl, client_id, scope, secureProxy.getRedirectUri(), state, nonce, null, null, null);

			Logd(TAG,"ocp: "+ocp.toString());
	        
	        RemoteListenerToken rl;
			synchronized(RemoteListenerTokenList) {
				rl = new RemoteListenerToken(listener);
				rl.ocp = ocp;
				RemoteListenerTokenList.add(rl);
			}

			// launch request
	        HttpOpenidConnect hc = new HttpOpenidConnect(ocp);

			if( ! hc.getTokens( Service.this, rl.id, null )  ) {
				setClientTokens(rl.id,null);
			}

			Logd(TAG,"getTokensWithOidcProxy end");
			
			return true;

		}

	    @Override
	    public String webFinger(
	            String userInput,
	            String serverUrl
	            ) {
			// check access
			if( !checkCallingSignature() ) return null;
			// check parameters
			if( isEmpty(serverUrl) || isEmpty(userInput) ) return null;
			try {
				return HttpOpenidConnect.webfinger(userInput, serverUrl);
			} catch (Exception e) {
				
			}
			return null;
		}
 
		@Override
		public String getUserInfo(
			String serverUrl,
			String access_token ) {
			// check access
			if( !checkCallingSignature() ) return null;
			// check parameters
			if( isEmpty(serverUrl) || isEmpty(access_token) ) return null;
			// do the job
			return HttpOpenidConnect.getUserInfo(serverUrl, access_token);
		}

		// Logout from the idp
		@Override
	    public boolean logout(String serverUrl) {
			// check access
			if( !checkCallingSignature() ) return false;
			// check parameters
			if( isEmpty(serverUrl) ) return false;
			// do the job
			return HttpOpenidConnect.logout(serverUrl);
		}

		// check calling process signature, if not valid return false
		// possibility of hack
		private boolean checkCallingSignature() {
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

		private void setTokensRedirect(String id, HttpOpenidConnect hc, String redirect) {
			// android.os.Debug.waitForDebugger();

			// check caller uid
			if( checkCallingUid() == false ) {
				// caller are not from inside this app
				Logd(TAG,"setTokensRedirect not from inside this app");
				return;
			}

			RemoteListenerToken rl=null;
			synchronized(RemoteListenerTokenList) {
				for(int i=RemoteListenerTokenList.size()-1; i>=0; i--) {
					RemoteListenerToken r = RemoteListenerTokenList.get(i);
					if(r.id.compareTo(id)==0) {
						rl = r;
						RemoteListenerTokenList.remove(i);
						break;
					}
				}
			}
			
			if( rl != null  ) {
		        try {
		        	String tokens = hc.doRedirect(redirect);

		        	JSONObject jObject = null;
		        	try {
		        		jObject = new JSONObject(tokens);
		        	} catch (JSONException je){
		        		// check if it is JWE
		        		tokens = secureProxy.decryptJWE( tokens );
		        		Logd(TAG,"doRedirect JWE: "+tokens);
		        		jObject = new JSONObject(tokens);
		        	}

		        	boolean user_cancel = false;
		        	String userCancel   = getFromJS( jObject, "cancel" );
		        	if(userCancel!=null && userCancel.equalsIgnoreCase("true") ) {
		        		user_cancel = true;
		        	}
		        	
		        	// put id_token and refresh_token in Secure Storage
		        	if(user_cancel==false) {
			        	String access_token  = getFromJS( jObject, "access_token" );
			        	// String token_type    = getFromJS( jObject, "token_type" );
			        	String refresh_token = getFromJS( jObject, "refresh_token" );
			        	String expires_in    = getFromJS( jObject, "expires_in" );
			        	String id_token      = getFromJS( jObject, "id_token" );
			        	rl.ocp.m_server_scope= getFromJS( jObject, "scope" );
		        		rl.listener.handleTokenResponseWithOidcProxy(id_token, access_token, false );
						return;


		        	} else {
		        		rl.listener.handleTokenResponseWithOidcProxy(EMPTY, EMPTY, true );
		        		return;
		        	}
		        	
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		        // if here, then nothing to notify
				if( rl.listener!=null ) {
					try {
		        		rl.listener.handleTokenResponseWithOidcProxy(EMPTY, EMPTY, false );
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}

		}
		
		@Override
		public void cancel(String id, boolean user) throws RemoteException {
			
			// check caller uid
			if( checkCallingUid() == false ) {
				// caller are not from inside this app
				Logd(TAG,"setTokens not from inside this app");
				return;
			}

			RemoteListenerToken rl=null;
			synchronized(RemoteListenerTokenList) {
				for(int i=RemoteListenerTokenList.size()-1; i>=0; i--) {
					RemoteListenerToken r = RemoteListenerTokenList.get(i);
					if(r.id.compareTo(id)==0) {
						rl = r;
						RemoteListenerTokenList.remove(i);
						break;
					}
				}
			}
			
			if( rl != null  ) {
        		rl.listener.handleTokenResponseWithOidcProxy(EMPTY, EMPTY, user );
			}

			// hideNotifIcon();
		}

		@Override
		public void doRedirect( String id, String redirect ) {
			
			// check caller uid
			if( checkCallingUid() == false ) {
				// caller are not from inside this app
				Logd(TAG,"doRedirect not from inside this app");
				return;
			}

			Logd(TAG,"doRedirect begin");
			
			if(id==null || id.length()==0 ) {
				Logd(TAG,"doRedirect end no ID");
				// hideNotifIcon();
				return;
			}
			
	        OpenidConnectParams ocp = null;
			// android.os.Debug.waitForDebugger();
			
			synchronized(RemoteListenerTokenList) {
				for(int i=RemoteListenerTokenList.size()-1; i>=0; i--) {
					RemoteListenerToken r = RemoteListenerTokenList.get(i);
					if(r.id.compareTo(id)==0) {
				        ocp = new OpenidConnectParams(r.ocp);
						break;
					}
				}
			}

			if( ocp != null ) {
		        HttpOpenidConnect hc = new HttpOpenidConnect(ocp);

		        try { 
		        	setTokensRedirect(id, hc, redirect );
		        	return;
		        } catch(Exception e) {
		        	e.printStackTrace();
		        }
			}
			
			// if error, set null response
	        try { 
	        	cancel(id,false);
	        } catch(Exception e) {
	        	e.printStackTrace();
	        }

	        Logd(TAG,"doRedirect end");
			// hideNotifIcon();
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

	void setClientTokens(String id, String tokens) {
		Logd(TAG,"setClientTokens id:"+id);
		Logd(TAG,"setClientTokens tokens:"+tokens);
		RemoteListenerToken rl=null;
		synchronized(RemoteListenerTokenList) {
			for(int i=RemoteListenerTokenList.size()-1; i>=0; i--) {
				RemoteListenerToken r = RemoteListenerTokenList.get(i);
				if(r.id.compareTo(id)==0) {
					rl = r;
					RemoteListenerTokenList.remove(i);
					break;
				}
			}
		}
		
		if( rl != null  ) {

        	JSONObject jObject = null;
	        try {
	        	if(tokens!=null)
	        		jObject = new JSONObject(tokens);

	        	if(jObject!=null) {
		        	String access_token  = getFromJS( jObject, "access_token" );
		        	// String token_type    = getFromJS( jObject, "token_type" );
		        	String refresh_token  = getFromJS( jObject, "refresh_token" );
		        	String expires_in     = getFromJS( jObject, "expires_in" );
		        	String id_token       = getFromJS( jObject, "id_token" );
		        	rl.ocp.m_server_scope = getFromJS( jObject, "scope" );
		        	boolean user_cancel = false;
		        	String userCancel   = getFromJS( jObject, "cancel" );
		        	if(userCancel!=null && userCancel.equalsIgnoreCase("true") ) {
		        		user_cancel = true;
		        	}
		        	
					rl.listener.handleTokenResponseWithOidcProxy( id_token, access_token, user_cancel );
					return;
	        	}
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
		if(rl!=null && rl.listener!=null ) {
			try {
        		rl.listener.handleTokenResponseWithOidcProxy(EMPTY, EMPTY, false );
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
	}
	
	
	// get a string from a json object
	String getFromJS(JSONObject jo, String name){
		if ( jo != null ) {
			try {
				return jo.getString(name);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	String sortScope(String scope) {
		// sort scope in alphabetical order
		if( scope != null ) {
    		scope = scope.toLowerCase(Locale.getDefault());
    		// offline_access is mandatory
    		if ( !scope.contains("offline_access") ) {
    			scope += " offline_access";
    		}

    		String scopes[] = scope.split("\\ ");
    		if(scopes!=null) {
    			Arrays.sort(scopes, new Comparator<String>() {
    				 @Override
    				 public int compare(String s1, String s2) {
    				    return s1.compareToIgnoreCase(s2);
    				    }
    				 });
				scope = null;
				// filter null or empty strings
    			for(int i=0; i<scopes.length; i++) {
    				if( scopes[i] != null && scopes[i].length()>0 ) {
	    				if(scope==null)
	    					scope = scopes[i];
	    				else
	    					scope += ( " " + scopes[i] );
    				}
    			}
    		}
		}
		return scope;
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

            .setContentTitle("SECURE OIDC PROXY")
            .setContentText("privacy protected")
            .setSmallIcon(R.drawable.masked_on)
            .setAutoCancel(false)
            .build();
        } else {
        	mNotification = new Notification.Builder(this)

            .setContentTitle("SECURE OIDC PROXY")
            .setContentText("privacy not protected")
            .setSmallIcon(R.drawable.masked_off)
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

	// return true is string is null or empty, false otherwise
    private static boolean isEmpty(String s) {
    	if(s==null || s.length()==0)
    		return true;
    	return false;
    }
}
