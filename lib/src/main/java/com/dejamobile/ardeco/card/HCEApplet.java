package com.dejamobile.ardeco.card;

/**
 * Created by Sylvain on 21/04/2015.
 */
public abstract class HCEApplet {

    public static void install(){

    }

    public abstract void processApdu(APDU apdu) throws Throwable;
}
