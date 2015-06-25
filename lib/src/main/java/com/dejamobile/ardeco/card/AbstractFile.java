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
 * Abstract Class for ISO7816 files implem
 * @author Sylvain
 * @version 1.0
 */
public abstract class AbstractFile {

    protected static final short SW_FILE_INVALIDATED = 0x6a83;

    public static final byte MODE_CREATE_FILE = 0;
    public static final byte MODE_READ_BINARY = 1;
    public static final byte MODE_UPDATE_BINARY = 2;
    public static final byte MODE_READ_RECORD = 3;
    public static final byte MODE_UPDATE_RECORD = 4;

    // link to parent DF
    protected DedicatedFile parentFile;
    // file size, different from file data Size
    private short fileSize;
    // file Access Conditions
    protected byte[] ac = new byte[4];
    // file identifier
    protected short fileID;
    private byte fileType;
    protected boolean active = true;

    public AbstractFile(short fid, DedicatedFile parent, byte[] ac) {
        fileID = fid;
        parentFile = parent;
        if (parent != null)
            parent.addSibling(this);
        Util.arrayCopy(this.ac, (short) 0, ac, (short) 0,
                (short) this.ac.length);
    }

    public DedicatedFile getParent() {
        return parentFile;
    }

    public void checkAccessAllowed(AbstractFile selectedFile, byte mode) {

        if (selectedFile instanceof DedicatedFile) {
            switch (mode) {
                case MODE_CREATE_FILE:
                    // TODO check current ac
                    break;
                case MODE_READ_BINARY:
                case MODE_UPDATE_BINARY:
                case MODE_READ_RECORD:
                case MODE_UPDATE_RECORD:
                    ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            }
        }

    }

    public abstract AbstractFile createFile(APDU apdu);

    public abstract void readBinary(APDU apdu);

    public abstract void updateBinary(APDU apdu);

    public abstract void readRecord(APDU apdu);

    public abstract void updateRecord(APDU apdu);

    public short getFileID() {
        return fileID;
    }

    public abstract short[] getPath();

    public void setActive(boolean b) {
        active = b;
    }

    public void setAc(byte[] ac) {
        this.ac = ac;
    }

    private AbstractFile findFirstInHierarchyByFid(short fid){
        AbstractFile f = null;
        AbstractFile current = this;
        if (current instanceof DedicatedFile){

            f=((DedicatedFile)current).getSibling(fid);

        }

        while(f==null){
            AbstractFile parent = current.getParent();
            if (parent == null)
                break;
            f= ((DedicatedFile)parent).getSibling(fid);
            current = parent;
        }
        return f;
    }


    public CHVFile getRelevantCHV1File(){
        return (CHVFile)findFirstInHierarchyByFid((short)0);
    }

    public CHVFile getRelevantCHV2File(){
        return (CHVFile) findFirstInHierarchyByFid((short)0x100);
    }

    @Override
    public String toString() {
        return Integer.toHexString(fileID);
    }
}
