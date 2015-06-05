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


import com.dejamobile.ardeco.card.DedicatedFile;
import com.dejamobile.ardeco.card.RecordFile;

/**
 * An implementation of Cyclic File as defined in ISO Specifications.
 * <br>
 * Record number 1 shall always retrieve the most recently updated record, number 2 the second most recent and so on.
 */
public class CyclicFile extends RecordFile
{

	/** this points to the (theoretical) oldest record in a new object */
	private static final byte INITIAL_OLDEST_RECORD_OFFSET = (byte)0;
	/** this points to the (theoretical) newest record in a new object */
	private static final byte INITIAL_NEWEST_RECORD_OFFSET = (byte)1;

	/**Offest used to calculate current newest record (record 1)*/
	private byte newestRecordOffset;

	/**Offset used to access the current oldest record (if N is the number of records in the file this will be record N)*/
	private byte oldestRecordOffset;


	/**
	 * Specify the number of records and the length of each record (all records
	 * are the same length)
	 * @param numberOfRecords the number of records in this file
	 * @param ac access conditions
	 * @param recordLength the length of each record
	 * @throws ISOException with reason code ISO7816.SW_CONDITIONS_NOT_SATISFIED (6985) 
	 * if numberOfRecords < 0 or recordLength < 0   
	 */
	CyclicFile(short fid, DedicatedFile parent, byte[] ac, byte numberOfRecords, byte recordLength) {
		super(fid, parent, ac, numberOfRecords);

		newestRecordOffset = INITIAL_NEWEST_RECORD_OFFSET;
		oldestRecordOffset = INITIAL_OLDEST_RECORD_OFFSET;

		for (byte i = INITIAL_OLDEST_RECORD_OFFSET; i < numberOfRecords; i++) {
			records[i] = new Record(recordLength);
		}
		// set current record pointer to be record 1 
		currentRecord = (byte)1;

	}

	/**
	 * Get a record with the specified record number
	 *
	 * @param recordNum record number
	 * @return the specified record
	 * @throws ISOException with reason code ISO7816.SW_RECORD_NOT_FOUND (6A83) 
	 * if the record number is not found
	 */
	byte[] getRecord(byte recordNum, byte mode) {

		if(recordNum == P1_CURRENT_MODE){

			recordNum = currentRecord;
		}
		byte recordToFind = (byte) ((byte)(recordNum + newestRecordOffset - (byte)1) % (byte)records.length);

		try {
			return records[recordToFind].record;
		} catch (ArrayIndexOutOfBoundsException e){
			ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
			//throw new ISOException(ISO7816.SW_RECORD_NOT_FOUND);
			// unreachable in JC impl but keeps compiler from complaining
			return null;
		}

	}

	/**
	 * This gives a reference to the oldest record,
	 * which may be written to, and shall be made the newest record.
	 * All other records will be shuffled.
	 *
	 */
	byte[] getNewRecordForUpdate(){
		// decrement newestRecordOffset
		if(newestRecordOffset == 0){
			newestRecordOffset += records.length;
		}
		newestRecordOffset--;

		// decrement oldestRecordOffset, wrapping it if it is already 0
		if(oldestRecordOffset == 0){
			oldestRecordOffset += records.length;
		}
		oldestRecordOffset--;

		// now we have a new 'newest record' we return a reference so it may be updated
		Util.arrayFillNonAtomic(records[newestRecordOffset].record, (byte)0, (byte)records[newestRecordOffset].record.length, (byte)0);
		return records[newestRecordOffset].record;

	}

	/**Returns the number of records in this Cyclic EF*/
	byte getSize(){
		return (byte)records.length;
	}

	public short[] getPath() {
		// TODO Auto-generated method stub
		return null;
	}

}

