/*..._......_......................._....._._......
....|.|....(_).....................|.|...(_).|.....
..__|.|.___._..__._._.__.___...___.|.|__.._|.|.___.
./._`.|/._.\.|/._`.|.'_.`._.\./._.\|.'_.\|.|.|/._.\
|.(_|.|..__/.|.(_|.|.|.|.|.|.|.(_).|.|_).|.|.|..__/
.\__,_|\___|.|\__,_|_|.|_|.|_|\___/|_.__/|_|_|\___|
.........._/.|.....................................
.........|__/
Copyright (C) 2015 dejamobile.
*/
package com.dejamobile.ardeco.card;

/**
 * Created by Sylvain on 21/04/2015.
 */
public class APDU {

    byte[] buffer;

    byte[] responseBuffer;

    private final byte[] SW_OK = new byte[]{(byte)0x90,0};

    byte cla, ins, p1, p2, Lc, Le;
    private short outgoingLength = 0;

    public APDU(byte[] buffer) {
        this.buffer = buffer;
        cla = buffer[ISO7816.OFFSET_CLA];
        ins = buffer[ISO7816.OFFSET_INS];
        p1 = buffer[ISO7816.OFFSET_P1];
        p2 = buffer[ISO7816.OFFSET_P2];
        if (buffer.length > 4) {
            Lc = buffer[ISO7816.OFFSET_LC] ;
            if (buffer.length == 5)
            Le = Lc;
        }
    }
    /*
     * Retrieves apdu buffer, dont clone
     */
    public byte[] getBuffer() {
        return buffer;
    }

    public byte getCla() {
        return cla;
    }

    public byte getIns() {
        return ins;
    }

    public byte getP1() {
        return p1;
    }

    public byte getP2() {
        return p2;
    }

    public byte getLc() {
        return Lc;
    }

    public byte getLe() {
        return Le;
    }

    public short setIncomingAndReceive() {
        return (short) (buffer.length - ISO7816.OFFSET_CDATA);
    }

    public short setOutgoing() {
        return Le;
    }

    public void setOutgoingLength(short outgoingLength) {
        this.outgoingLength = outgoingLength;
    }

    public void sendBytesLong(byte[] data, short offset, short le) {
        responseBuffer = new byte[outgoingLength + 2];
        System.arraycopy(data,offset,responseBuffer,0,le);
        System.arraycopy(SW_OK,0,responseBuffer,le,SW_OK.length);
    }

    public byte[] getResponse(){
        if (outgoingLength > 0){
            return responseBuffer;
        }else{
            return SW_OK;
        }
    }
}
