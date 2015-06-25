package com.dejamobile.ardeco.card;




public class FixedLinearFile extends RecordFile {
	
	

	public FixedLinearFile(short fid, DedicatedFile parent, byte[] ac, byte numberOfRecords, byte recordLength) {
		super(fid, parent, ac, numberOfRecords);		
		
		for (byte i = 0; i < numberOfRecords; i++) {
			records[i] = new Record(recordLength);
		}
	}
	
	public byte[] getRecordData(byte recordNumber) {
		if (active == true){
			if (numberOfRecords <= recordNumber)
				ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
			currentRecord = recordNumber;
			return records[recordNumber].record;
		}
		else {
			ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);			
		}
		return null;
	}

	public short[] getPath() {
		// TODO Auto-generated method stub
		return null;
	}

}
