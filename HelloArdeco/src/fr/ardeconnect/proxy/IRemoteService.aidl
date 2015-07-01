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
