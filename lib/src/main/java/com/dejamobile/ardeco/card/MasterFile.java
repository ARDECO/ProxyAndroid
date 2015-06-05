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

public class MasterFile extends DedicatedFile {
	
	// Can be only one master file
	private static MasterFile instance;
	
	// Get the only one Master file Instance
	public static MasterFile getInstance() {
		if (instance== null)
			instance=new MasterFile();
		return instance;
	}
	
	private static final short MF_FID = 0x3F00;
	private MasterFile() {
		// file identifier of MasterFile is hard coded to 3F00	
		// Access conditions are hard coded for MF
		super(MF_FID,new byte[]{0,0,0,0});
	}
}
