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
 * Encapsulates a byte array to allow implementation of multiple-record files.
 * <br>Java Card does not support 2D or higher order arrays but will allow arrays of objects.
 */
public class Record
{
	/**The encapsulated array holding the record contents*/
	public byte[] record;

	/**
	 * Returns object with the specified record length 
	 * @param recordLength the desired length of the record
	 * @throws NegativeArraySizeException if recordLength is negative
	 */	
    public Record(short recordLength) {
      record = new byte[recordLength];
    }

}
