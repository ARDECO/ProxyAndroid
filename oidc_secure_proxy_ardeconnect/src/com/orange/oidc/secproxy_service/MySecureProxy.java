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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

/**
 * 
 * SDCardStorage class
 * used to generate crypted and signed request objects
 *
 */
public class MySecureProxy extends SecureProxy {
	
	protected static final String TAG = "SD Storage";

	final static String alg = "RS256";

	// JWS signature header
	static final String signHeader = "{\"alg\":\""+alg+"\",\"kid\":\"k2bdc\"}";

	// client_id and client_secret
	// here for demo only, must be stored in a secure element
	static final private String SECURE_PROXY_client_id = "SECURE_PROXY";
    static final private String SECURE_PROXY_secret = "securesecret";

    static final private String SECURE_PROXY_redirect_uri = "http://secure_proxy/";

	public String getClientId() {
		return SECURE_PROXY_client_id ;
	}

	public String getRedirectUri() {
		return SECURE_PROXY_redirect_uri ;
	}

    // get request object 
	public String getOidcRequestObject(
			String server_url,
			String client_id,
			String scope,
			PublicKey serverPubKey
			) {

		JSONObject object = new JSONObject();
		try {
			object.put("response_type", "code");
			object.put("scope", scope);
			object.put("redirect_uri", getRedirectUri());

			object.put("client_id", SECURE_PROXY_client_id);
		
			JSONObject JSO = new JSONObject();
			JSO.put("app_id", new JSONObject().put("value", client_id));
			object.put("secure_proxy", JSO);

			Logd(TAG, "getOidcRequestObject : "+object.toString());
			
			String requestParam64 = KryptoUtils.encodeB64(object.toString().getBytes());

			// get JWT
			String jwS = KryptoUtils.signJWS(requestParam64, signHeader, alg, RsaKeyProxy.privRsaKey);
			// jwS = object.toString();
			byte jwsBytes [] = jwS.getBytes();
			short paddLeft = (short) (jwsBytes.length % 16);
			if(paddLeft>0) {
				byte padd = (byte) ( 16 - paddLeft );
				byte jwsBytes_tmp [] = new byte [jwsBytes.length+padd];
				System.arraycopy(jwsBytes, 0, jwsBytes_tmp, 0, jwsBytes.length);
				for(byte i=0; i<padd; i++) {
					jwsBytes_tmp[(short)(jwsBytes.length+i)] = padd;
				}
				jwsBytes = jwsBytes_tmp;
			}

			// encrypt JWT request by JWE
			return KryptoUtils.encryptJWE(jwsBytes, serverPubKey, null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public String getPrivateKeyJwt(String token_endpoint) {
		String privateKeyJwt = null;
        try {
            JSONObject jo = new JSONObject();
			jo.put("iss", SECURE_PROXY_client_id);
			jo.put("sub", SECURE_PROXY_client_id);
			jo.put("aud", token_endpoint);
			jo.put("jti", new BigInteger(130, new SecureRandom()).toString(32));
			long now = Calendar.getInstance().getTimeInMillis() / 1000;
			// expires in 3 minutes
			jo.put("exp", ""+(now+180));
		
			String dataToSign=null;
			try {
				dataToSign = KryptoUtils.encodeB64(jo.toString().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			if( dataToSign!=null && dataToSign.length()>0) {
				// sign with proxy private key
	            String signH = "{\"alg\":\""+alg+"\"}";
	            privateKeyJwt = KryptoUtils.signJWS(dataToSign, signH, alg, RsaKeyProxy.privRsaKey);
			}

        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return privateKeyJwt;
	}
	
	public String getClientSecretBasic() {
		
		String bearer = (SECURE_PROXY_client_id+":"+SECURE_PROXY_secret);
        return Base64.encodeToString(bearer.getBytes(),Base64.DEFAULT);
	}

	public String decryptJWE(String jwe) {
		try {
			return new String(KryptoUtils.decryptJWE(jwe, RsaKeyProxy.privRsaKey),"UTF-8");
		} catch(Exception e) {}
		return null;
	}

	
	static void Logd(String tag, String msg) {
		if(tag==null) tag = "unknown";
		if(msg==null) msg = "unknown";
		Log.d(tag, msg);
	}

	static void Logerr(String tag, String msg) {
		if(tag==null) tag = "unknown";
		if(msg==null) msg = "unknown";
		Log.e(tag, msg);
	}

	// RSA key used by the Secure Proxy
	// here for demo only, must be stored in a secure element
	final static class RsaKeyProxy {

		// RSA 512
		static private String _rsaNs = "10003190979582651350655152244004345890631015214818181537786763230713471231853499869807883467394056677198764505889961899609697526623820585440838924851122131";
		static private String _rsaDs = "8266671093492170791331355502010701946024013672193611426927248677471376503611314512710786540172282771004573025670257705631085683716365421687136951057439233";
		static private String _rsaEs = "65537";

		static public  RSAPrivateKey privRsaKey;
		static public  PublicKey pubRsaKey;
	}

	// init the key from the big numbers above
	static {
		BigInteger rsaN = null;
		BigInteger rsaE = null;
		BigInteger rsaD = null;
		try {
			rsaN = new BigInteger(RsaKeyProxy._rsaNs);
			rsaD = new BigInteger(RsaKeyProxy._rsaDs);
			rsaE = new BigInteger(RsaKeyProxy._rsaEs);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		
		RSAPrivateKeySpec privRsaSpec = new RSAPrivateKeySpec(rsaN, rsaD);
		RSAPublicKeySpec pubRsaSpec = new RSAPublicKeySpec(rsaN, rsaE);
		RsaKeyProxy.pubRsaKey = null;
		RsaKeyProxy.privRsaKey = null;
		try {
			KeyFactory keyfact = KeyFactory.getInstance("RSA","SC");
			RsaKeyProxy.pubRsaKey = keyfact.generatePublic(pubRsaSpec);
	        
        	KeyFactory kfactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec kspec = (RSAPublicKeySpec) kfactory.getKeySpec(RsaKeyProxy.pubRsaKey, RSAPublicKeySpec.class);
			
			RsaKeyProxy.privRsaKey = (RSAPrivateKey) keyfact.generatePrivate(privRsaSpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
