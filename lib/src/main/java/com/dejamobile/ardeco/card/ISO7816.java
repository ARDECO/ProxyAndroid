package com.dejamobile.ardeco.card;

/**
 *
 * @author Sylvain
 */
interface ISO7816 {
    short OFFSET_CLA = 0;
    short OFFSET_INS = 1;
    short OFFSET_P1 = 2;
    short OFFSET_P2 = 3;
    short OFFSET_LC = 4;
    short OFFSET_CDATA = 5;
    
    short SW_WRONG_DATA = 0x6a80;
    short SW_CONDITIONS_NOT_SATISFIED = 0x6985;
    short SW_RECORD_NOT_FOUND = 0x6a83;
    short SW_NO_ERROR = (short)0x9000;

    short SW_COMMAND_NOT_ALLOWED = 0x6982;
    short SW_DATA_INVALID = 0x6a80;
    short SW_WRONG_LENGTH = 0x6700;
    short SW_WRONG_P1P2 = 0x6b00;
    short SW_CORRECT_LENGTH_00 = 0x6100;
    short SW_SECURITY_STATUS_NOT_SATISFIED = 0x6a82;
    short SW_INCORRECT_P1P2 = 0x6a86;
    short SW_INS_NOT_SUPPORTED = 0x6d00;
    short SW_FILE_NOT_FOUND = 0x6a82;
    short SW_FILE_INVALID = 0x6983;
    short SW_WRONG_PIN_0_TRIES_LEFT = 0x6300;

    short SW_CLA_NOT_SUPPORTED = 0x6E00 ;
}
