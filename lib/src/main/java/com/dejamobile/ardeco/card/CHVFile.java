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


public class CHVFile extends ElementaryFile {

	private static final byte PIN_SIZE = 8;
	public static final byte INFO_PIN_VALUE = (byte) 0;
	public static final byte INFO_UNBLOCK_PIN_VALUE = (byte) 1;
	public static final byte INFO_PIN_REMAINING_ATTEMPS = (byte) 2;
	public static final byte INFO_UNBLOCK_PIN_REMAINING_ATTEMPS = (byte) 3;

	byte keyAlgo = 0;
	byte triesRemaining = 0;

	byte remainingAttemps = 0;

	public CHVFile(short fid, DedicatedFile parent, byte[] ac, byte[] d) {
		super(fid, parent, ac, d);
	}

	public CHVFile(short fid, DedicatedFile parent, byte[] ac, short maxSize) {
		super(fid, parent, ac, maxSize);
	}

	/**
	 * Retrieves CHV Material
	 * @param dataBuffer byte array where to write info
	 * @param dataBufferOffset offset in dest buffer
	 * @param info what to retrieve
	 * 
	 */
	public void retrieveCHVData(byte[] dataBuffer, short dataBufferOffset,
			 byte info) {

		switch (info) {
		case INFO_PIN_VALUE:
			Util.arrayCopy(data, (short) 3, dataBuffer,
					dataBufferOffset, (short) PIN_SIZE);
			break;
		case INFO_UNBLOCK_PIN_VALUE:
			Util.arrayCopy(data, (short) 13, dataBuffer,
					dataBufferOffset, (short) PIN_SIZE);
			break;
		}
	}
	
	public boolean check(byte[] pinBuffer, short pinOffset){
		byte checked = Util.arrayCompare(data, (short) 3, pinBuffer, pinOffset, PIN_SIZE);
		if (checked != (byte)0){
			data[12] = (byte) (data[12]-1);
			
		}else{
			data[12] = data[11];
		}
		return checked ==(byte)0;
	}
	
	public byte getTriesRemaining(){
		return data[12];
	}

}
