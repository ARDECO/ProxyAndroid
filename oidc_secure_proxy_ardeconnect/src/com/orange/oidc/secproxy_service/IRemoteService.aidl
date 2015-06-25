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

import com.orange.oidc.secproxy_service.IRemoteListenerToken;

/**
 *
 */
interface IRemoteService {

    // public functions

    /**
    * ask tokens credential to a OIDC PROXY enable AS ( Authorization Server )
    *
    * @param listener
    *           the token listener callback instance
    * @param serverUrl
    *           the URL of the authorization server
    * @param client_id
    *           the ID of client application on the authorization server
    * @param scope
    *           the information the application want to access
    *           @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#ScopeClaims">OpenID Connect Core 1.0 documentation / Scope Claims</a>
    * @param state
    *           RECOMMENDED. Opaque value used to maintain state between the request and the callback.
    *           @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest">OpenID Connect Core 1.0 documentation / Auth Request</a>
    * @param nonce
    *           OPTIONAL. String value used to associate a Client session with an ID Token, and to mitigate replay attacks.
    *           @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest">OpenID Connect Core 1.0 documentation / Auth Request</a>
    *           
    * @return
    *           a first status on the operation, false for incorrect parameters or access error
    */
    boolean getTokensWithOidcProxy(
        IRemoteListenerToken listener,
        String serverUrl,
        String client_id,
        String scope, 
        String state, 
        String nonce );

    /**
    * WebFinger discovers information for a URI that might not be usable as a locator otherwise, such as account or email URIs. 
    * to determine the host server.
    * @param userInput
    *           the data given by the user ( ie an email ) ( must not be null )
    * @param serverUrl
    *           the URL of the resource server, if not null, will request directly on the server
    * 
    * @return
    *           the server URL, null if error or not found
    *
    */
    String webFinger(
        String userInput,
        String serverUrl
        );



    /**
    * getUserInfo returns the userInfo request on an oidc server 
    * @param serverUrl
    *           the URL of the resource server
    * @param access_token
    *           previously provided access_token
    * 
    * @return
    *           the user info in json format
    *
    */
    String getUserInfo(
        String serverUrl,
        String access_token
        );

    /**
    * logout end session on the server 
    * @param serverUrl
    *           the URL of the server
    * 
    * @return
    *           the status on the logout, false if error
    *
    */
    boolean logout(String serverUrl);
}
