package com.dejamobile;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Helper pour effectuer des conversions entre repr�sentation hexad�cimale et
 * repr�sentation sous forme d'octets.
 *
 * @author Sylvain Decourval
 *
 */
public class ConvertUtils {

    /**
     * reverses One buffer content
     * @param src
     * @param srcOffset
     * @param dest
     * @param destOffset
     * @param length 
     */
    public static void reverseBuffer(byte[] src, short srcOffset, byte[] dest, short destOffset, short length) {
        short i;
        short l;
        for (i = 0; i < length; i = (short) (i + 2)) {
            l = (short) ((length - 1) - i + srcOffset);
            dest[(short) (i + destOffset)] = src[l];
            dest[(short) (i + destOffset + 1)] = src[(short) (l - 1)];
        }
    }

    /**
     * Used to test the "numberness" of a String <br>
     * Coding may be Int, BCD, HEX <br>
     *
     * @param value
     * @return true when String is interpreted as a number represenattion, false
     * otherwise
     */
    public static boolean isNumber(String value) {

        return Pattern.matches("^[0-9A-F]*$", value.toUpperCase());

    }

    public static byte[] swap(final byte[] buffer) {

        if (buffer == null) {
            return null;
        }

        byte[] retArray = Arrays.copyOf(buffer, buffer.length);

        for (int i = 0; i < retArray.length; i++) {
            retArray[i] = nibbleSwap(retArray[i]);
        }

        return retArray;

    }

    private static BitSet fromByteArray(byte[] bytes, boolean littleEndian) {
        BitSet bits = new BitSet();
        if (littleEndian) {
            for (int i = 0; i < bytes.length * 8; i++) {
//                System.out.println("Scanning Byte : " + i/8);
                if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
//                    System.out.println("Setting bit : " + i);
                    bits.set(i);
                }
            }
        } else {
            for (int i = 0; i < bytes.length * 8; i++) {
//             System.out.println("Scanning Byte : " + i/8);
                if ((bytes[i / 8] & (0x80 >> (i % 8))) > 0) {
//                System.out.println("Setting bit : " + i);
                    bits.set(i);
                }
            }
        }
        return bits;
    }

    /**
     * Returns a bitset containing the values in bytes. The byte-ordering of
     * bytes must be little-endian which means the least significant byte is
     * element 0. For exemple value A01243EE will be stored as EE 43 12 A0 with
     * least significant bit first
     *
     * @param bytes
     * @return the resultant Bit Set
     */
    public static BitSet fromLittleEndianByteArray(byte[] array) {
        return fromByteArray(array, true);
    }

    /**
     * Returns a bitset containing the values in bytes. The byte-ordering of
     * bytes must be big-endian which means the most significant bit is in
     * element 0. For exemple value A01243EE wil be stored as A0 12 43 EE
     *
     * @param bytes
     * @return the resultant Bit Set
     */
    public static BitSet fromBigEndianByteArray(byte[] array) {
        return fromByteArray(array, false);
    }

    /**
     * Returns a byte array of at least length 1. The most significant bit in
     * the result is guaranteed not to be a 1 (since BitSet does not support
     * sign extension). The byte-ordering of the result is big-endian which
     * means the most significant bit is in element 0. The bit at index 0 of the
     * bit set is assumed to be the least significant bit.
     *
     * @param bits
     * @return resultant byte Array
     */
    public static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    /**
     * Converts a byte array to a long
     *
     * @param arr
     * @param start
     * @return
     */
    public static long byteArray2long(byte[] arr, int start) {
        int i = 0;


        if (arr == null || (start >= arr.length) || start < 0) {
            return 0;
        }
        int len = arr.length;
        long accum = 0;
        i = 0;
        while (len > 0) {
            len--;
            accum += ((long) (arr[i++] & 0xff)) << len * 8;
        }
        return accum;
    }

    /**
     * Converts a boolean bitmap to a byte value
     *
     * @param bitmap
     * @return
     */
    public static byte bitmap2Byte(boolean[] bitmap) {
        byte value = 0;

        if (bitmap.length > 8) {
            throw new IllegalArgumentException("Invalid bitmap size, must be < 8");
        }

        for (int i = 0; i < bitmap.length; i++) {
            value += (bitmap[i]) ? (1 << bitmap.length - i - 1) : 0;
        }
        return value;
    }

    /**
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
        };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /**
     * Converts a byte to hex digit
     */
    public static String toHexString(byte b) {
        StringBuffer buf = new StringBuffer();
        byte2hex(b, buf);
        return buf.toString();
    }

    /**
     * Converts a byte array to hex string.
     */
    public static String toHexString(byte[] block) {
        return toHexString(block, null);
    }

    /**
     * Converts a byte array to hex string using guven separator.
     */
    public static String toHexString(byte[] block, String separator) {
        StringBuffer buf = new StringBuffer();

        if (block == null) {
            return "";
        }
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (separator != null && i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

    /**
     * Converts a byte array to hex string using guven separator.
     */
    public static String toHexString(byte[] block, String separator, int start, int offset) {
        StringBuffer buf = new StringBuffer();

        if (block == null) {
            return "null";
        }
        int len = block.length;

        if ((offset + start) > len) {
            offset = len - start;
        }
        for (int i = start; i < offset + start; i++) {
            byte2hex(block[i], buf);
            if (separator != null && i < len - 1) {
                buf.append(separator);
            }
        }
        return buf.toString();
    }

    /**
     * Converts a byte array to hex string using guven separator.
     */
    public static String toFormattedHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();

        if (block == null) {
            return "null";
        }
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if ((i + 1) % 16 == 0) {
                buf.append("\n");
            } else if (i < len - 1) {
                buf.append(" ");

            }

        }
        return buf.toString();
    }

    /**
     * Checks if the string contains only ASCII printable characters.
     *
     * <code>null</code> will return
     * <code>false</code>. An empty String ("") will return
     * <code>true</code>.
     *
     * <pre>
     * StringUtils.isAsciiPrintable(null)     = false
     * StringUtils.isAsciiPrintable("")       = true
     * StringUtils.isAsciiPrintable(" ")      = true
     * StringUtils.isAsciiPrintable("Ceki")   = true
     * StringUtils.isAsciiPrintable("ab2c")   = true
     * StringUtils.isAsciiPrintable("!ab-c~") = true
     * StringUtils.isAsciiPrintable("\u0020") = true
     * StringUtils.isAsciiPrintable("\u0021") = true
     * StringUtils.isAsciiPrintable("\u007e") = true
     * StringUtils.isAsciiPrintable("\u007f") = false
     * StringUtils.isAsciiPrintable("Ceki G\u00fclc\u00fc") = false
     * </pre>
     *
     * @param str the string to check, may be null
     * @return <code>true</code> if every character is in the range 32 thru 126
     * @since 2.1
     */
    public static boolean isAsciiPrintable(byte[] str) {
        if (str == null) {
            return false;
        }
        int sz = str.length;
        for (int i = 0; i < sz; i++) {
            if (isAsciiPrintable(str[i]) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the character is ASCII 7 bit printable.
     *
     * <pre>
     *   CharUtils.isAsciiPrintable('a')  = true
     *   CharUtils.isAsciiPrintable('A')  = true
     *   CharUtils.isAsciiPrintable('3')  = true
     *   CharUtils.isAsciiPrintable('-')  = true
     *   CharUtils.isAsciiPrintable('\n') = false
     *   CharUtils.isAsciiPrintable('&copy;') = false
     * </pre>
     *
     * @param ch the character to check
     * @return true if between 32 and 126 inclusive
     */
    private static boolean isAsciiPrintable(byte ch) {
        return ch >= 32 && ch < 127;
    }

    /**
     * converting to hexstring to bytes
     *
     * @param strhex the hexString
     * @return Le tableau d'octet corrsepondant
     */
    public static byte[] hex2byte(String strhex) {

        if (strhex == null) {
            throw new IllegalArgumentException("Converted String value must not be null");
        }
        strhex = strhex.replaceAll("\n", "");

        strhex = strhex.replaceAll(" ", "");

        StringBuilder buf = new StringBuilder();
        if (strhex.contains("|")) {
            StringTokenizer stringTokenizer = new StringTokenizer(strhex, "|");
            while (stringTokenizer.hasMoreTokens()) {
                String token = stringTokenizer.nextToken();
                if (ConvertUtils.hex2byte(token) == null) {
                    token = ConvertUtils.toHexString(token.getBytes());
                }
                buf.append(token);
            }
        }
        if (buf.length() != 0) {
            strhex = buf.toString();
        }

        if (strhex == null) {
            return null;
        }
        int l = strhex.length();
        if (l == 1) {
            strhex = "0" + strhex;
            l = strhex.length();
        }

        if (l % 2 == 1) {
            return null;
        }
        byte[] b = new byte[l / 2];
        try {
            for (int i = 0; i < l / 2; i++) {
                b[i] = (byte) Integer.parseInt(strhex.substring(i * 2, i * 2 + 2), 16);
            }
        } catch (NumberFormatException e) {
            b = null;
        }
        return b;
    }

    public static int toInt(byte[] buffer) {
        int value = 0;
        for (int j = 0; j < buffer.length; j++) {
            value += (buffer[(buffer.length - 1) - j] & 0xff) << (8 * j);
        }
        return value;
    }

    /**
     * Converts a part of a byte array to an int value
     *
     * @param buffer buffer where bytes are found
     * @param offset where to start
     * @param length length to convert (number of bytes)
     */
    public static int toInt(byte[] buffer, int offset, int length) {
        int value = 0;
        for (int j = 0; j < length; j++) {
            value += (buffer[(length + offset - 1) - j] & 0xff) << (8 * j);
        }
        return value;
    }

    public static boolean checkApdusLength(String[] apdus, int maxLength) {
        for (int i = 0; i < apdus.length; i++) {
            //vérification de longueur maximum
            if (apdus[i].length() > maxLength * 2) {
                return false;
            }
            //vérification de parité
            if ((apdus[i].length() % 2) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Splits apdu string
     *
     * @param apdu
     * @param splitLength
     * @return
     */
    public static String[] splitApdu(String apdu, int splitLength) {
        ArrayList<String> cmd = new ArrayList<String>();
        int currentIdx = 0;
        while (currentIdx < apdu.length()) {
            if (currentIdx + splitLength * 2 <= apdu.length()) {
                cmd.add(apdu.substring(currentIdx, currentIdx + splitLength * 2));
            } else {
                cmd.add(apdu.substring(currentIdx));
            }
            currentIdx += splitLength * 2;
        }
        String[] result = new String[cmd.size()];
        cmd.toArray(result);
        return result;
    }

    /**
     * Gets a nibble value within a byte array <br>
     * First nibble (leftmost) will be <br>
     * <code>
     * int startRank = (array.length*2) -1;
     * </code> Last nibble (rightmost) will be 0
     *
     * @param array Byte array containing nibble
     * @param nibbleRank The nibble rank
     * @return the nibble value as an int
     * @throws IllegalArgumentException when targeted nibble is out of range
     * @throws IllegalArgumentException when array is null
     */
    public static int getNibbleValue(byte[] array, int nibbleRank) {

        if (array == null) {
            throw new IllegalArgumentException("Array must not be null");
        }

        int retNibble = 0;
        int startRank = (array.length * 2) - 1;

        if (nibbleRank > startRank || nibbleRank < 0) {
            throw new IllegalArgumentException("Targeted nibble is out of range. Must be between 0 and " + ((array.length * 2) - 1));
        }
        for (byte b : array) {
            retNibble = (b >>> 4) & 0x0f;
            if (nibbleRank == startRank) {
                break;
            }
            startRank--;
            retNibble = b & 0x0f;
            if (nibbleRank == startRank) {
                break;
            }
            startRank--;
        }
        return retNibble;
    }

    private static byte nibbleSwap(byte inByte) {
        int nibble0 = (inByte << 4) & 0xf0;
        int nibble1 = (inByte >>> 4) & 0x0f;
        return (byte) ((nibble0 | nibble1));
    }
}
