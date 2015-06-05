/*..._......_......................._....._._......
....|.|....(_).....................|.|...(_).|.....
..__|.|.___._..__._._.__.___...___.|.|__.._|.|.___.
./._`.|/._.\.|/._`.|.'_.`._.\./._.\|.'_.\|.|.|/._.\
|.(_|.|..__/.|.(_|.|.|.|.|.|.|.(_).|.|_).|.|.|..__/
.\__,_|\___|.|\__,_|_|.|_|.|_|\___/|_.__/|_|_|\___|
.........._/.|.....................................
.........|__/
Copyright (C) 2014 dejamobile.
*/
package com.dejamobile.ardeco.card;

/**
 * Record File specific
 * @author Sylvain
 * @version 1.0
 * @see AbstractFile
 * @see CyclicFile
 */
public abstract class RecordFile extends AbstractFile {

    /**P1 byte indicating read or write to Current Record*/
    protected static final byte P1_CURRENT_MODE = (byte)0;
    /**
     * Read, Update Modes P2 Values
     */
    protected final static byte P2_ABSOLUTE_MODE = 4;
    protected final static byte P2_CURRENT_MODE_FIRST = 0;
    protected final static byte P2_CURRENT_MODE_LAST = 1;
    protected final static byte P2_CURRENT_MODE_NEXT = 2;
    protected final static byte P2_CURRENT_MODE_PREVIOUS = 3;

    /**Holds the records as an array of objects (this because JC does not support multidimensional arrays*/
    protected Record[] records;
    /**
     * Records pointer
     */
    protected byte currentRecord;

    protected byte numberOfRecords;

    public RecordFile(short fid, DedicatedFile parent, byte[] ac, byte numberOfRecords) {
        super(fid, parent, ac);
        if (numberOfRecords <= 0 ) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        this.numberOfRecords = numberOfRecords;
        records = new Record[numberOfRecords];
    }

    public AbstractFile createFile(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        return parentFile;
    }

    public void readBinary(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);

    }

    public void updateBinary(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);

    }

    public void readRecord(APDU apdu) {
        if (!active)
            ISOException.throwIt(SW_FILE_INVALIDATED);
        byte recordNumber = apdu.getBuffer()[ISO7816.OFFSET_P1];
        byte mode = apdu.getBuffer()[ISO7816.OFFSET_P2];
        // inform the JCRE that the applet has data to return
        short le = apdu.setOutgoing();

        // write selected record in APDU
        byte[] record = getRecord(recordNumber,
                mode);
        apdu.setOutgoingLength((short) record.length);
        apdu.sendBytesLong(record, (short) 0, (short) record.length);
    }

    public void updateRecord(APDU apdu) {
        if (!active)
            ISOException.throwIt(SW_FILE_INVALIDATED);
        byte recordNumber = apdu.getBuffer()[ISO7816.OFFSET_P1];
        byte mode = apdu.getBuffer()[ISO7816.OFFSET_P2];
        short byteRead = apdu.setIncomingAndReceive();
        // check Lc
        short lc = (short) (apdu.getBuffer()[ISO7816.OFFSET_LC] & 0x00FF);
        if ((lc == 0) || (byteRead == 0))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        // update file
        updateRecord(apdu.getBuffer(),ISO7816.OFFSET_CDATA, lc, recordNumber, mode);

    }

    /**
     * Get a record with the specified record number
     *
     * @param recordNum record number
     * @return the specified record
     * @throws ISOException with reason code ISO7816.SW_RECORD_NOT_FOUND (6A83)
     * @throws ISOException with reason code ISO7816.SW_INCORRECT_P1P2 (6A86)
     * if the record number is not found
     */
    private byte[] getRecord(byte recordNum, byte mode) {

        if (mode > (byte)4 || mode <0)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if(recordNum == P1_CURRENT_MODE){

            recordNum = currentRecord;
            switch (mode){
                case P2_CURRENT_MODE_FIRST:
                    recordNum=0;
                    break;
                case P2_CURRENT_MODE_LAST:
                    recordNum=(byte) (records.length-1);
                    break;
                case P2_CURRENT_MODE_NEXT:
                    recordNum++;
                    break;
                case P2_CURRENT_MODE_PREVIOUS:
                    recordNum--;
                    break;
                default:
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }
        }else{
            // recordIndex == recordNum -1
            recordNum--;
        }
        currentRecord = recordNum;
        try {
            return records[recordNum].record;
        } catch (ArrayIndexOutOfBoundsException e){
            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
            //throw new ISOException(ISO7816.SW_RECORD_NOT_FOUND);
            // unreachable in JC impl but keeps compiler from complaining
            return null;
        }

    }

    void updateRecord(byte[] data, short offset, short len,byte recordNum, byte mode){
        //SElect current record and check length
        if (getRecord(recordNum, mode).length != len)
            ISOException.throwIt((short) (ISO7816.SW_WRONG_LENGTH + records[currentRecord].record.length));
        Util.arrayCopy(data, offset, records[currentRecord].record, (short)0, len);
    }

    public short[] getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getRecordById(byte recorId){
        return records[recorId].record;
    }

}