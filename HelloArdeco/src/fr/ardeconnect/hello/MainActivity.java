package fr.ardeconnect.hello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import fr.ardeconnect.hello.R;
import fr.ardeconnect.proxy.IRemoteListener;
import fr.ardeconnect.proxy.IRemoteService;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

// Main display
public class MainActivity extends Activity {

	protected static final String TAG = MainActivity.class.getName();

	// oidc server connection parameters
	String serverUrl     = null;
	String scope         = "";
	String state         = "";
	String nonce         = "";
	String client_id     = "";

	// returned results from service
	String m_sub = null;
	String m_user_info = null;

	String loginBtnTitle = "login with Ardeco Proxy";
	
	// service connection 
	RemoteServiceConnection connection;
	// remote service 
	IRemoteService service;

	/**
	 * This class represents the actual service connection. It casts the bound
	 * stub implementation of the service to the AIDL interface.
	 */
	class RemoteServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder boundService) {
			// connection established, get remote service object
			service = IRemoteService.Stub.asInterface(boundService);
			
			Log.d(TAG, "onServiceConnected() connected");
			toast("Service Ardeco connected",1);
			
			loginBtn.setText(loginBtnTitle);
		}

		public void onServiceDisconnected(ComponentName name) {
			// connection closed
			service = null;
			
			loginBtn.setText("connect");
			
			Log.d(TAG, "onServiceDisconnected() disconnected");
			toast("Service Ardeco disconnected",1);
		}
	}

	// hide login and progress bar buttons
	private void hideButtons() {
		loginBtn.setVisibility(View.GONE);
		pb.setVisibility(View.GONE);
	}
	
	// Binds this activity to the service
	private boolean initService() {
		connection = new RemoteServiceConnection();
/*
        try {
    		connectionInternal = new RemoteServiceConnectionInternal();
        	bindService(new Intent(IRemoteServiceInternal.class.getName()),
                connectionInternal, Context.BIND_AUTO_CREATE);
        } catch (SecurityException se) {
        	toast("Security Error :\nnot allowed to connect to the service internal", 2);
        } catch (Exception e) {
        	e.printStackTrace();
        }
*/
        boolean bRet = false;
        try {
        	bRet = bindService(new Intent(IRemoteService.class.getName()),
                connection, Context.BIND_AUTO_CREATE);
        } catch (SecurityException se) {
        	toast("Security Error :\nnot allowed to connect to the service", 2);
        } catch (Exception e) {
        	e.printStackTrace();
        }

		Log.d(TAG, "initService() bound result: " + bRet);

        return bRet;
	}
	
	// Unbinds this activity from the service.
	private void releaseService() {
		if (connection != null) {
			unbindService(connection);
			connection = null;
			Log.d(TAG, "releaseService() unbound.");
		}
	}

	// ui objects
	Button loginBtn;

	TextView txtHello;
	WebView  webView;
	ProgressBar pb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Setup the UI
		webView = (WebView) findViewById(R.id.webView);
		txtHello = (TextView) findViewById(R.id.textHello);
		pb = (ProgressBar) findViewById(R.id.progressBar);
		loginBtn = (Button) findViewById(R.id.login);

		hideButtons ();
		
		loadSettings();
		
		if( m_sub==null || m_sub.length()==0 ) {
			loginBtn.setVisibility(View.VISIBLE);
		} else {
			getSP_UserInfo();
		}

		// connect to the service
		if( initService() == false ) {
			addToWebview("<font color=\"red\">Service not connected</font><br>");
		}

		// set action listener
		loginBtn.setText(loginBtnTitle);
		loginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (service == null) {
					// connect to the service if not connected
					initService();
				} else {
					// do the request
					doLogin();
				}
			}
		});
	}

	// load parameters from shared preferences
	void loadSettings() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		serverUrl     = sharedPrefs.getString("server_url",    serverUrl);
		m_sub	      = sharedPrefs.getString("sub",           null);
	}

	// show menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	final int SETTINGS_RETURN_CODE = 152124;
	
	// menu events
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.action_settings) {
			startActivityForResult(new Intent(this, SettingsActivity.class),SETTINGS_RETURN_CODE);
		} else if(item.getItemId() == R.id.action_logout) {
			m_sub = "";
			saveSub();
			loginBtn.setVisibility(View.VISIBLE);
			txtHello.setText("");
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SETTINGS_RETURN_CODE ) {
			loadSettings();
		}
	}
	
	/** Called when the activity is about to be destroyed. */
	@Override
	protected void onDestroy() {
		// clean up service on activity ending
		releaseService();
		super.onDestroy();
	}

	// request tokens to the service
	void doLogin() {
		Log.d(TAG, "doLogin");
		
		// show progress bar animation
		pb.setVisibility(View.VISIBLE);

		// toast("asking new tokens", 1);
		addToWebview("enrolling...");
		
		// do in a different thread to not block UI
		new Thread() {
			@Override
			public void run() {
				try {
					// serviceInternal.resetCookies();

					// call the remote service with specified parameters
					if (service != null) {
						Log.d(TAG, "doLogin : launch get tokens");
						Log.d(TAG, "doLogin : client_id=" + client_id );
						
						
						if( service.enrollWithArdeco(
								remoteListener,
								serverUrl ) == false ) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// hide progress bar animation
									pb.setVisibility(View.GONE);
									addToWebview("<font color=\"red\">error enroll</font>");
								}});
							
						}
						Log.d(TAG, "doLogin : waiting tokens");
					}
				} catch (final Exception e) {
					Log.d(TAG, "doLogin failed with: " + e);
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// hide progress bar animation
							pb.setVisibility(View.GONE);
							addToWebview("<font color=\"red\">"+e.getMessage()+"</font>");
						}});
				}
			}
		}.start();
	}

	// save access_token value to shared preferences
	void saveSub() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPrefs.edit();
		editor.putString("sub", m_sub);
		editor.commit();
	}
	
	// request user info via service
	void getSP_UserInfo() {
		// show progress bar animation
		// update ui with result
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				pb.setVisibility(View.VISIBLE);
			}
		});

		// do in a different thread to not block UI
		new Thread() {
			@Override
			public void run() {
				try {
					String ui = getHttpString(serverUrl+"?get="+m_sub);
					if(!isEmpty(ui)) {
						m_user_info = ui;
					}
					
				} catch (Exception e) {
					Log.d(TAG, "doLogin failed with: " + e);
					e.printStackTrace();
				}
				// update ui with result
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						txtHello.setText("Hello "+m_user_info);
						pb.setVisibility(View.GONE);
					}
				});
			}
		}.start();
	}

	// token response callback from service
	private IRemoteListener.Stub remoteListener = new IRemoteListener.Stub() {

		@Override
		public     void handleSpCode (
		        String spCode,
		        Bundle spParameter,
		        final String errorMessage,
		        final boolean user_cancel
		        ) throws RemoteException {
			
			
		
			Log.d(TAG,"handleTokenResponseWithOidcProxy : "+spCode);
			System.out.println("handleTokenResponse");

			// memorize result
			if( spCode != null && spCode.length() > 0 ) {
				if( spCode.startsWith("sub=") ) {
					m_sub = spCode.substring(4);
					saveSub();
				}
			}
			
			if( m_sub!=null && m_sub.length()>0 ) {

				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						loginBtn.setVisibility(View.GONE);
						addToWebview("<font color=\"green\">enrolled</font>");
					}
				});
				
				Log.d(TAG, "cookie : " + spParameter.getString(new String("sessionSecret")));
				checkTickets(spParameter.getString(new String("sessionSecret")));
//				getSP_UserInfo();
			} else {
				// get user info
				runOnUiThread(new Runnable() {
	
					@Override
					public void run() {
						if( user_cancel ) {
							addToWebview("<font color=\"red\">user canceled</font>");
							pb.setVisibility(View.GONE);
						} else if( errorMessage == null || errorMessage.length() == 0) {
							addToWebview("<font color=\"red\">Error : "+errorMessage+"</font>");
							pb.setVisibility(View.GONE);
						}
					}
				});
			}
		}

	};





	private void checkTickets(String cookie) {


		String myUrl = "http://piscine.ardeconnect.fr/tickets"; 
		CookieSyncManager.createInstance(this); 
		CookieManager cookieManager = CookieManager.getInstance(); 
		//Cookie sessionCookie =  getCookie(); 
		cookieManager.setCookie("piscine.ardeconnect.fr", cookie); 
		
		WebView webView = (WebView) findViewById(R.id.webView); 
		webView.getSettings().setBuiltInZoomControls(true); 
		webView.getSettings().setJavaScriptEnabled(true); 
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl(myUrl);
		
	}


	
	// display toast to screen
	void toast(String msg, int duration) {
		android.widget.Toast.makeText(
				MainActivity.this,
				msg,
				duration == 0 ? android.widget.Toast.LENGTH_SHORT : android.widget.Toast.LENGTH_LONG ).show();
	}

	// display event history in webview
	String html="";
	void addToWebview(String msg) {
		html += msg + "<br>";
        webView.loadData(html, "text/html", "UTF8");
	}
	
	// get a text resource from an URL
	static String getHttpString(String url) {

		String result = null;
		
		// build connection
        HttpURLConnection huc = getHUC(url);
        huc.setInstanceFollowRedirects(false);

        try {
	        // try to establish connection 
           huc.connect();
           // get result
           int responseCode = huc.getResponseCode();
           Log.d(TAG, "getHttpString response: "+responseCode);
           
           // if 200, read http body
           if ( responseCode == 200 ) {
               InputStream is = huc.getInputStream();
               result= convertStreamToString(is);
               is.close();
           } else {
        	   // result = "response code: "+responseCode;
           }
           
           // close connection
           huc.disconnect();
	        
		} catch (Exception e) {
            Log.e(TAG, "revokeSite FAILED");
			e.printStackTrace();
		}
        
		return result;
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
