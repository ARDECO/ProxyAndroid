// IRemoteService.aidl
package fr.ardeconnect.proxy;

interface IRemoteListener {

    /**
    * callback handler to a prior enroll request
    *
    * @param spCode
    *           a code return by the service provide, null if canceled or on error
    * @param errorMessage
    *           error message if any
    * @param user_cancel
    *           true if user cancelled the operation, false otherwise
    *           
    */
    void handleSpCode (
        String spCode,
        String errorMessage,
        boolean user_cancel
        );
}