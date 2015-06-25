/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dejamobile.ardeco.card;

/**
 *
 * @author Sylvain
 */
public class ISOException extends RuntimeException {

    private short code;

    static void throwIt(short e) {
        System.err.println("Throw ex : " + e);
        ISOException ie = new ISOException();
        ie.setCode(e);
        throw ie;
    }

    public  short getCode() {
        return this.code;
    }

    public  void setCode(short code) {
        this.code = code;
    }

    public byte[] getAsByteArray(){
        return new byte[]{(byte) ((getCode() >> 8) & (byte)0xff), (byte) (getCode() & (byte)0xff)};
    }

    public ISOException() {
    }

    public ISOException(Throwable cause) {
        super(cause);
    }
    
    
    private ISOException(String string) {
        super(string);
    }
    
}
