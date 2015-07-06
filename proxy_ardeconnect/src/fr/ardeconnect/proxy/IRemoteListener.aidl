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

// IRemoteService.aidl
package fr.ardeconnect.proxy;

interface IRemoteListener {

    /**
    * callback handler to a prior enroll request
    *
    * @param spCode
    *           a code return by the service provide, null if canceled or on error
    * @param spParameter
    *           contains information from SP : secret allowing to access session
    * @param errorMessage
    *           error message if any
    * @param user_cancel
    *           true if user cancelled the operation, false otherwise
    *           
    */
    void handleSpCode (
        String spCode,
        inout Bundle spParameter,
        String errorMessage,
        boolean user_cancel
        );
}