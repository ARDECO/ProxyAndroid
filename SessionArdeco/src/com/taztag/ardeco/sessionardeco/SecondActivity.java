/******************************************************************************
 ** TAZTAG -  All Rights Reserved.
  ******************************************************************************/

package com.taztag.ardeco.sessionardeco;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class SecondActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		
		Log.d(TAG, "onCreate");
		
		final Intent intent = getIntent();
		
		new Thread() {
			public void run() {
				treatsIntent(intent);
			}
		}.start();
			
	}

	private String TAG = SecondActivity.class.getName();
	
	protected void treatsIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");
		String cookie = intent.getStringExtra("cookie");
		
		if ( (cookie != null ) && cookie.startsWith("laravel_session=")) {

			String cookieValue = cookie.replaceFirst("laravel_session=", "");
	
			Log.d(TAG, "cookie value = " + cookieValue);
			
			 URL url;
			 HttpURLConnection urlConnection = null;
			try {
				url = new URL("http://piscine.ardeconnect.fr/tickets");
				urlConnection = (HttpURLConnection) url.openConnection();

				urlConnection.setRequestProperty("Cookie", cookie);
				
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				
				String response = convertStreamToString(in);

				WebView webview = (WebView)this.findViewById(R.id.webView1);
				webview.getSettings().setJavaScriptEnabled(true);
				webview.loadDataWithBaseURL("", response, "text/html", "UTF-8", "");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  finally {
				if (urlConnection != null)
					urlConnection.disconnect();
			}
	
			  
			  
			
		}
	}

	
    public static String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {        
            return "";
        }
    }


	
	
	
	
	
}

