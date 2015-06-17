
package com.dejamobile.ardeco.card;


public class ElementaryFile extends AbstractFile {

	// data stored in file
	protected byte[] data;
	// current size of data stored in file
	short size;

	public ElementaryFile(short fid, DedicatedFile parent, byte[] ac, byte[] d) {
		super(fid, parent, ac);
		data = d;
		size = (short) d.length;
	}

	public ElementaryFile(short fid, DedicatedFile parent, byte[] ac,
			short maxSize) {
		super(fid, parent, ac);
		data = new byte[maxSize];
		size = maxSize;
	}

	public DedicatedFile getParent() {
		return parentFile;
	}

	public void readBinary(APDU apdu) {
		if (!active)
			ISOException.throwIt(SW_FILE_INVALIDATED);
		// use P1 and P2 as offset
		short offset = Util.makeShort(apdu.getBuffer()[ISO7816.OFFSET_P1], apdu
				.getBuffer()[ISO7816.OFFSET_P2]);
		// inform the JCRE that the applet has data to return
		short le = (short)(0xff & apdu.setOutgoing());
		// impossible to start reading from offset large than size of file

		if (offset > getMaxSize())
			ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
		// number of bytes in file starting from offset
		short remaining = (short) (getMaxSize() - offset);
		if (le == 0) {
			if (remaining < 256) {
				// wrong Le field
				// SW2 encodes the exact number of available data bytes
				short sw = (short) (ISO7816.SW_CORRECT_LENGTH_00 | remaining);
				ISOException.throwIt(sw);
			} else
				// Le = 0 is interpreted as 256 bytes
				le = 256;
		}
		// only read out the remaining bytes
		if (le > remaining) {
			le = remaining;
		}
		// set the actual number of outgoing data bytes
		apdu.setOutgoingLength(le);
		// write selected file in APDU
		apdu.sendBytesLong(getData(), offset, le);
	}

	public void updateBinary(APDU apdu) {
		if (!active)
			ISOException.throwIt(SW_FILE_INVALIDATED);

		short offset = Util.makeShort(apdu.getBuffer()[ISO7816.OFFSET_P1], apdu
				.getBuffer()[ISO7816.OFFSET_P2]);
		// impossible to start updating from offset larger than max size of file
		// this however does not imply that the file length can not change
		short size = getMaxSize();
		if (offset > size)
			ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
		// number of bytes in file starting from offset
		// short remaining = (short) (size - offset);
		// get the new data
		short byteRead = apdu.setIncomingAndReceive();
		// check Lc
		short lc = (short) (apdu.getBuffer()[ISO7816.OFFSET_LC] & 0xFF);
		if (lc < 0)
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		if ((lc == 0) || (byteRead == 0))
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		// update file

		// Manage specific files chv1 chv2
		

		updateData(offset, apdu.getBuffer(), ISO7816.OFFSET_CDATA, lc);

	}

	public AbstractFile createFile(APDU apdu) {
		ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
		return parentFile;
	}

	public void readRecord(APDU apdu) {
		ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
	}

	public void updateRecord(APDU apdu) {
		ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
	}

	public byte[] getData() {
		if (active == true)
			return data;
		else {
			ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
			return null;
		}
	}

	public short getCurrentSize() {
		if (active == true)
			return size;
		else {
			ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
			return 0;
		}
	}

	public short getMaxSize() {
		return (short) data.length;
	}

	public short[] getPath() {
		short[] path = parentFile.getPath();
		path[(short) (path.length + 1)] = getFileID();
		return path;
	}

	public void updateData(short dataOffset, byte[] newData,
			short newDataOffset, short length) {
		// update size
		size = (short) (dataOffset + length);
		// copy new data
		
		Util.arrayCopy(newData, newDataOffset, data, dataOffset, length);
	}
}
