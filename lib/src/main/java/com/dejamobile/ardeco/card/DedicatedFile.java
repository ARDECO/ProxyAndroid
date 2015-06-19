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


public class DedicatedFile extends AbstractFile {

    private static final int MAX_FILES_ALLOWED = 0x7f;

    private static final short ACTIVATED_FLAG_OFFSET = 9 + ISO7816.OFFSET_CDATA;
    private static final short FILE_ID_OFFSET = 2 + ISO7816.OFFSET_CDATA;
    private static final short FILE_SIZE_OFFSET = ISO7816.OFFSET_CDATA;
    private static final short FILE_TYPE_OFFSET = 4 + ISO7816.OFFSET_CDATA;
    private static final short RECORD_SIZE_OFFSET = 14 + ISO7816.OFFSET_CDATA;
    private static final byte FILE_TYPE_DF = 0x08;
    private static final byte FILE_TYPE_CYCLIC = 0x06;
    private static final byte FILE_TYPE_VAR_RECORD = 0x04;
    private static final byte FILE_TYPE_FIXED_RECORD = 0x02;
    private static final byte FILE_TYPE_TRANSPARENT = 0x01;

    // Creation ac buffer
    private byte[] tmpAC = new byte[4];
    // link to parent DF
    private DedicatedFile parentFile;
    // list of sibling files (either EF or DF)
    private static final byte MAX_SIBLINGS = 10;
    private AbstractFile[] siblings = new AbstractFile[MAX_SIBLINGS];
    // number of siblings
    private byte number = 0;

    // constructor only used by MasterFile
    protected DedicatedFile(short fid, byte[] ac) {
        // MasterFile does not have a parent, as it is the root of all files
        super(fid, null, ac);

    }

    public DedicatedFile(short fid, DedicatedFile parent, byte[] ac) {
        super(fid, parent, ac);
    }

    public AbstractFile createFile(APDU apdu) {

        byte numberOfRecords = apdu.getBuffer()[ISO7816.OFFSET_P2];

        short byteRead = apdu.setIncomingAndReceive();
        // check Lc
        short lc = (short) (apdu.getBuffer()[ISO7816.OFFSET_LC] & 0x00FF);
        if ((lc == 0) || (byteRead == 0))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        //
        // TODO check Values
        short fileSize = Util.getShort(apdu.getBuffer(), FILE_SIZE_OFFSET);

        short fid = Util.getShort(apdu.getBuffer(), FILE_ID_OFFSET);
        // check DF has reached max files
        if (siblings.length == MAX_FILES_ALLOWED)
            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
        // check fid already existing
        byte i = 0;
        while (siblings[i] != null) {
            if (siblings[i].getFileID() == fid)
                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
            i++;
        }
        byte fileType = apdu.getBuffer()[FILE_TYPE_OFFSET];


        Util.arrayCopy(apdu.getBuffer(), (short) 7, tmpAC, (short) 0,
                (short) tmpAC.length);

        boolean activated = apdu.getBuffer()[ACTIVATED_FLAG_OFFSET] == 1;

        AbstractFile createdFile = null;
        AbstractFile returnedFile = this;


        byte recordSize = 0;

        switch (fileType) {
            case FILE_TYPE_TRANSPARENT:
                if (fid == 0 || fid == 0x100) {
                    createdFile = new CHVFile(fid, this, tmpAC, fileSize);
                } else {
                    createdFile = new ElementaryFile(fid, this, tmpAC, fileSize);
                }

                break;
            case FILE_TYPE_VAR_RECORD:
                createdFile = new VariableLinearFile(fid, this, tmpAC, numberOfRecords);
                break;
            case FILE_TYPE_CYCLIC:
                recordSize = apdu.getBuffer()[RECORD_SIZE_OFFSET];
                createdFile = new CyclicFile(fid, this, tmpAC, numberOfRecords, recordSize);
                break;
            case FILE_TYPE_DF:
                createdFile = new DedicatedFile(fid, this, tmpAC);
                returnedFile = createdFile;
                break;
            case FILE_TYPE_FIXED_RECORD:
                recordSize = apdu.getBuffer()[RECORD_SIZE_OFFSET];
                createdFile = new FixedLinearFile(fid, this, tmpAC, numberOfRecords, recordSize);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        createdFile.setActive(activated);

        return returnedFile;

    }

    public void readBinary(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
    }

    public void updateBinary(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
    }

    public void readRecord(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
    }

    public void updateRecord(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
    }

    public short[] getPath() {
        short[] path;
        if (parentFile != null) {
            path = parentFile.getPath();
            path[(short) (path.length + 1)] = getFileID();
        } else
            path = new short[]{getFileID()};
        return path;
    }

    public DedicatedFile getParent() {
        return parentFile;
    }

    public byte getNumberOfSiblings() {
        return number;
    }

    public AbstractFile getSibling(short fid) {
        for (byte i = 0; i < getNumberOfSiblings(); i++) {
            if (siblings[i].getFileID() == fid)
                return siblings[i];
        }
        return null;
    }

    protected void addSibling(AbstractFile s) {
        if (number < MAX_SIBLINGS)
            siblings[number++] = s;
    }

    public AbstractFile[] getSiblings() {
        return siblings;
    }
}
