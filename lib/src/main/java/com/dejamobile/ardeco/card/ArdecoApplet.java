package com.dejamobile.ardeco.card;

import android.util.Log;

import java.util.Random;

/**
 * Created by Sylvain on 04/06/2015.
 */
public class ArdecoApplet extends HCEApplet {

    protected final static short MF = (short) 0x3F00;
    protected final static short EF_CHV1 = (short) 0x0000;
    protected final static short EF_CHV2 = (short) 0x0100;
    protected final static short EF_KEY_EXT = (short) 0x0011;
    protected final static short EF_KEY_INT = (short) 0x0001;
    protected final static short EF_ATR = (short) 0x2F01;
    protected final static short EF_ICC_SN = (short) 0x0002;
    protected final static byte PIN_SIZE = 8;
    protected final static byte CHV1_PIN = (byte) 0x01;
    protected final static byte CARDHOLDER_PIN_TRY_LIMIT = 3;
    protected final static byte CHV2_PIN = (byte) 0x02;
    private final static byte ARDECO_CLA_2 = (byte) 0x80;
    private final static byte ARDECO_CLA_1 = (byte) 0x00;
    // codes of INS byte in the command APDUs
    private final static byte INS_GET_RESPONSE = (byte) 0xC0;
    private final static byte INS_SELECT_FILE = (byte) 0xA4;
    private final static byte INS_ACTIVATE_FILE = (byte) 0x44;
    private final static byte INS_DEACTIVATE_FILE = (byte) 0x04;
    private final static byte INS_READ_BINARY = (byte) 0xB0;
    private final static byte INS_UPDATE_BINARY = (byte) 0xD6;
    private final static byte INS_READ_RECORD = (byte) 0xB2;
    private final static byte INS_UPDATE_RECORD = (byte) 0xDC;
    private final static byte INS_VERIFY_PIN = (byte) 0x20;
    private final static byte INS_CHANGE_PIN = (byte) 0x24;
    private final static byte INS_VERIFY_KEY = (byte) 0x2A;
    private final static byte INS_GET_CHALLENGE = (byte) 0x84;
    private final static byte INS_INTERNAL_AUTHENTICATE = (byte) 0x88;
    private final static byte INS_EXTERNAL_AUTHENTICATE = (byte) 0x82;
    private final static byte INS_CREATE_FILE = (byte) 0xE0;
    private final static byte VERIFY_CARDHOLDER_PIN = (byte) 0x01;
    private final static byte OFFSET_PIN_HEADER = ISO7816.OFFSET_CDATA;
    private final static byte OFFSET_PIN_DATA = ISO7816.OFFSET_CDATA + 1;
    private static final String TAG = ArdecoApplet.class.getName();
    protected MasterFile masterFile = MasterFile.getInstance();
    private byte[] randomBuffer = new byte[256];
    private byte[] tempBuffer = new byte[256];
    // file selected by SELECT FILE; defaults to the MF
    private AbstractFile selectedFile;
    private byte previousApduType;

    @Override
    public void processApdu(APDU apdu) throws Throwable {

        byte[] buffer = apdu.getBuffer();

        if (buffer[ISO7816.OFFSET_CLA] == ARDECO_CLA_1) {
            // check the INS
            switch (buffer[ISO7816.OFFSET_INS]) {

                case INS_VERIFY_PIN:
                    verifyPin(apdu, buffer);
                    break;
                case INS_GET_CHALLENGE:
                    getChallenge(apdu, buffer);
                    break;
                case INS_EXTERNAL_AUTHENTICATE:
                    //externalAuthenticate(apdu);
                    break;
                case INS_SELECT_FILE:
                    selectFile(apdu, buffer);
                    break;
                case INS_CREATE_FILE:
                    selectedFile = selectedFile.createFile(apdu);
                    break;
                case INS_READ_BINARY:
                    selectedFile.readBinary(apdu);
                    break;
                case INS_READ_RECORD:
                    selectedFile.readRecord(apdu);
                    break;
                case INS_UPDATE_BINARY:
                    selectedFile.updateBinary(apdu);
                    break;
                case INS_UPDATE_RECORD:
                    selectedFile.updateRecord(apdu);
                    break;
                default:
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                    break;
            }
            Log.d(TAG, "Selected file is : " + selectedFile);
        } else {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

    }

    /**
     * select a file on the ARDECO card
     */
    private void selectFile(APDU apdu, byte[] buffer) {
        // check P2
        if (buffer[ISO7816.OFFSET_P2] != (byte) 0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        // P1 determines the select method
        switch (buffer[ISO7816.OFFSET_P1]) {
            case (byte) 0x00:
                selectByFileIdentifier(apdu, buffer);
                break;
            case (byte) 0x04:
                selectByDFName(apdu, buffer);
                break;
            case (byte) 0x08:
                selectByPath(apdu, buffer);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                break;
        }

        // default FCI in command response temporary buffer
        byte[] fci = {(byte) 0x6F, (byte) 0x04, (byte) 0x83, (byte) 0x02, (byte) 0x00, (byte) 0x00};
        Util.arrayCopy(fci, (short) 0, tempBuffer, (short) 0, (short) fci.length);
        Util.setShort(tempBuffer, (short) (4), selectedFile.getFileID());
        short sendLength = (short) fci.length;

        if (selectedFile instanceof DedicatedFile) {
            Log.d(TAG, "Selected file is a DF : " + selectedFile);
            DedicatedFile df = (DedicatedFile) selectedFile;

            Log.d(TAG, "DF has " + df.getNumberOfSiblings() + " siblings.");
            tempBuffer[1] = (byte) (4 + 2 + (df.getNumberOfSiblings() * 4));
            tempBuffer[6] = (byte) 0x85; // Tag 85, Proprietary information, list of file
            tempBuffer[7] = (byte) (df.getNumberOfSiblings() * 4);
            sendLength = (short) (8 + (df.getNumberOfSiblings() * 4));
            if (df.getNumberOfSiblings() > 0) {
                for (byte i = 0; i < df.getNumberOfSiblings(); i++) {
                    AbstractFile af = df.getSiblings()[i];
                    tempBuffer[8 + (i * 4)] = (byte) 0x83;  //Tag
                    tempBuffer[8 + (i * 4) + 1] = (byte) 0x2;  //Length
                    Util.setShort(tempBuffer, (short) (8 + (i * 4) + 2), af.getFileID());
                }
                Log.d(TAG, "FCI will be sent");
            }
        }
        apdu.setOutgoingLength(sendLength);
        apdu.sendBytesLong(tempBuffer, (short) 0, sendLength);

    }

    /**
     * select file by DF name (here select this APP)
     */
    private void selectByDFName(APDU apdu, byte[] buffer) {
        // receive the data to see which file needs to be selected
        short byteRead = apdu.setIncomingAndReceive();
        // check Lc
        short lc = (short) (buffer[ISO7816.OFFSET_LC] & 0x00FF);
        byte[] appletAid = new byte[]{(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x45, (byte) 0x41, (byte) 0x72, (byte) 0x64, (byte) 0x65, (byte) 0x63, (byte) 0x6F};
        if (lc != appletAid.length) {
            ISOException.throwIt(ISO7816.SW_FILE_INVALID);
        }
        byte b = Util.arrayCompare(buffer, ISO7816.OFFSET_CDATA, appletAid, (short) 0, (byte) lc);
        if (b != 0) {
            ISOException.throwIt(ISO7816.SW_FILE_INVALID);
        }

        selectedFile = masterFile;
    }

    /**
     * select file under the current DF
     */
    private void selectByFileIdentifier(APDU apdu, byte[] buffer) {
        // receive the data to see which file needs to be selected
        short byteRead = apdu.setIncomingAndReceive();
        // check Lc
        short lc = (short) (buffer[ISO7816.OFFSET_LC] & 0x00FF);
        if ((lc != 2) || (byteRead != 2))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        // get the file identifier out of the APDU
        short fid = Util.makeShort(buffer[ISO7816.OFFSET_CDATA],
                buffer[ISO7816.OFFSET_CDATA + 1]);
        // if file identifier is the master file, select it immediately
        AbstractFile s = null;
        if (fid == MF)
            selectedFile = masterFile;

        else {

            if (selectedFile instanceof DedicatedFile) {
                // check if the requested file exists under the current DF
                s = ((DedicatedFile) selectedFile).getSibling(fid);
                if (s != null)
                    selectedFile = s;
                else {
                    ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                }
            } else {
                AbstractFile pointer = selectedFile.getParent();// check parents

                if (pointer.getFileID() == fid) {
                    selectedFile = pointer;
                } else {

                    do {
                        s = ((DedicatedFile) pointer).getSibling(fid);
                        if (s == null)
                            pointer = pointer.getParent();
                        else
                            break;
                        if (pointer == null)
                            break;
                    } while (true);

                    if (s != null)
                        selectedFile = s;
                    else {
                        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                    }
                }
            }
        }
    }

    /**
     * select file by path from the MF
     */
    private void selectByPath(APDU apdu, byte[] buffer) {

        short byteRead = apdu.setIncomingAndReceive();

        short lc = (short) (buffer[ISO7816.OFFSET_LC] & 0x00FF);

        if (((lc & 1) == 1) || ((byteRead & 1) == 1))
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        // use the path name in the APDU data to select a file
        AbstractFile f = masterFile;
        for (byte i = 0; i < lc; i += 2) {
            short fid = Util.makeShort(
                    buffer[(short) (ISO7816.OFFSET_CDATA + i)],
                    buffer[(short) (ISO7816.OFFSET_CDATA + i + 1)]);
            // MF en tete ?
            if ((i == 0) && (fid == MF))
                f = masterFile;
            else {
                if ((f instanceof ElementaryFile) || f == null)
                    ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                f = ((DedicatedFile) f).getSibling(fid);
            }
        }
        if (f == null)
            ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
        selectedFile = f;
    }

    /**
     * verify the PIN
     */
    private void verifyPin(APDU apdu, byte[] buffer) {
        // check P1
        if (buffer[ISO7816.OFFSET_P1] != (byte) 0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        // receive the PIN data for validation
        apdu.setIncomingAndReceive();
        CHVFile chvFile = null;
        // check PIN depending on value of P2
        switch (buffer[ISO7816.OFFSET_P2]) {
            case CHV1_PIN:

                chvFile = selectedFile.getRelevantCHV1File();
                if (chvFile == null)
                    ISOException.throwIt((short) 0x6981);
                // overwrite previous APDU type
                setPreviousApduType(VERIFY_CARDHOLDER_PIN);
                // check the cardholder PIN

                break;


            default:
                ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        }
        checkPin(chvFile, buffer);
    }

    /**
     * generate a random challenge
     */
    private void getChallenge(APDU apdu, byte[] buffer) {
        // check P1 and P2
        if (buffer[ISO7816.OFFSET_P1] != (byte) 0x00
                || buffer[ISO7816.OFFSET_P2] != (byte) 0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        // inform the JCRE that the applet has data to return
        short le = apdu.setOutgoing();
        // Le = 0 is not allowed
        if (le == 0)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        setPreviousApduType(INS_GET_CHALLENGE);
        new Random().nextBytes(randomBuffer);
        // set the actual number of outgoing data bytes
        apdu.setOutgoingLength(le);
        // send content of buffer in apdu
        apdu.sendBytesLong(randomBuffer, (short) 0, le);
    }

    /**
     * check the PIN
     */
    private void checkPin(CHVFile pin, byte[] buffer) {

        byte tries = pin.getTriesRemaining();

        if (tries == 0) {
            // if the cardholder PIN is no longer valid (too many tries)
            ISOException.throwIt(ISO7816.SW_FILE_INVALID);
        }

        if (pin.check(buffer, OFFSET_PIN_HEADER) == true)
            return;

		/*
         * create the correct exception the status word is of the form 0x63Cx
		 * with x the number of tries left
		 */
        short sw = (short) (ISO7816.SW_WRONG_PIN_0_TRIES_LEFT | pin.getTriesRemaining());
        ISOException.throwIt(sw);
    }

    /**
     * return the previous APDU type
     */
    private byte getPreviousApduType() {
        return previousApduType;
    }

    /**
     * set the previous APDU type to a certain value
     */
    private void setPreviousApduType(byte type) {
        previousApduType = type;
    }
}
