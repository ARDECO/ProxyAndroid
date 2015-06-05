/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dejamobile.ardeco.card;

import com.dejamobile.ConvertUtils;

import java.util.Arrays;

/**
 *
 * @author Sylvain
 */
public class Util {

    static short makeShort(byte first, byte second) {
        short v = 0;
        v = (short) ((first & 0xff) * 256);
        v+=second & 0xff;
        System.out.println("Short build : " + Integer.toHexString(v & 0xffff));
        return v;
    }
    
    static short setShort(byte[] out, short offset, short value){
        out[offset++] = (byte) ((value >> 8)& 0xff);
        out[offset] = (byte) (value & 0xff);
        return 2;
    }
    
    static short arrayCopy(byte[] src, short srcOffset, byte[] dst, short dstOffset, short len){
        System.out.println("Array Copy "  + ConvertUtils.toHexString(src, null, srcOffset, len));
        System.arraycopy(src, srcOffset, dst, dstOffset, len);
        return len;
    }

    public static void arrayFillNonAtomic(byte[] buffer, short offset, short length, byte b) {
        for(short i = offset; i < (offset + length); i++ ){
            buffer[i] = b;
        }
    }

    public static short getShort(byte[] buffer, short fileSizeOffset) {
        return makeShort(buffer[fileSizeOffset], buffer[fileSizeOffset + 1]);
    }

    public static byte arrayCompare(byte[] data, short offset, byte[] otherData, short otherDataOffset, byte pinSize) {
        byte[] buffer1 = new byte[pinSize];
        byte[] buffer2 = new byte[pinSize];
        byte ret = 0;

        System.arraycopy(data, offset, buffer1,0, buffer1.length);
        System.arraycopy(otherData, otherDataOffset, buffer2,0, buffer2.length);

        if(!Arrays.equals(buffer1, buffer2)){
            ret = -1;
        }
        return ret;
    }
}
