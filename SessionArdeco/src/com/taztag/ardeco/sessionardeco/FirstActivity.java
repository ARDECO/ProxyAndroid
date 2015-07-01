/******************************************************************************
 ** TAZTAG -  All Rights Reserved.
 ******************************************************************************/

package com.taztag.ardeco.sessionardeco;

import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.Activity;
import android.content.Intent;

public class FirstActivity extends Activity {

	WebView webView;
	
	private String TAG = FirstActivity.class.getName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first);
		
		webView = (WebView) findViewById(R.id.webView1);

		webView.setWebViewClient(new WebViewClient()
	    {
	        @Override
	        public void onPageFinished(WebView view, String url)
	        {
	            super.onPageFinished(view, url);
	  
	            // Success
	            if (url.startsWith(new String("http://piscine.ardeconnect.fr/cb?code"))) {
		            CookieManager mgr = CookieManager.getInstance();
		            
		            String cookie = mgr.getCookie("piscine.ardeconnect.fr");
		            
		            if (cookie.startsWith(new String("laravel_session"))) {
		            
		            	Log.i(TAG, "Cookie : " + cookie);
			            Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
			            intent.putExtra("cookie", cookie);
		            	startActivity(intent);
		            
		            }
		            
	            }
	        }
	    }); 
		
		String url = "http://idp.ardeconnect.fr/oidc/authorize?response_type=code&client_id=piscine&redirect_uri=http%3A%2F%2Fpiscine.ardeconnect.fr%2Fcb&scope=identity&state=1234567890&language=en";
			
		webView.loadUrl(url);
	}

}
