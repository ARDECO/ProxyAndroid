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

import fr.ardeconnect.proxy.IRemoteListener;

/**
 *
 */
interface IRemoteService {

    // public functions

    /**
    * ask tokens credential to a OIDC PROXY enable AS ( Authorization Server )
    *
    * @param listener
    *           the response listener callback instance
    * @param spEnrollUrl
    *           the enroll URL of the service provider 
    *           
    * @return
    *           a first status on the operation, false for incorrect parameters or access error
    */
    boolean enrollWithArdeco(
        IRemoteListener listener,
        String spEnrollUrl);

}
