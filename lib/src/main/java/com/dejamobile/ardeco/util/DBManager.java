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
package com.dejamobile.ardeco.util;

import android.content.Context;

import com.dejamobile.ardeco.card.MasterFile;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;


/**
 * Created by Sylvain on 29/05/2015.
 */
public class DBManager {

    private static DBManager instance;

    private Context context;

    private DB fsDb;

    private DBManager(Context context) {
        this.context = context;
    }

    public static DBManager getInstance(Context context){
        if(instance==null){
            instance = new DBManager(context);
        }
        return instance;
    }

    public void openDB() throws SnappydbException {
        fsDb = DBFactory.open(context);
    }

    public MasterFile retrieveFileSystem() throws SnappydbException{
        if (fsDb.exists("fs")) {
            MasterFile mf = fsDb.getObject("fs", MasterFile.class);
            fsDb.close();
            return mf;
        }
        return null;
    }

    public void storeFileSystem() throws SnappydbException {
        fsDb.put("fs", MasterFile.getInstance());
        fsDb.close();
    }

}
