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

    private static final String TAG = DBManager.class.getCanonicalName();

    public static final String ARDECO_DB = "ARDECO_DB";
    public static final String FS_KEY = "fs";
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
        if (fsDb == null || !fsDb.isOpen()) {
            fsDb = DBFactory.open(context, ARDECO_DB);
        }
    }

    public MasterFile retrieveFileSystem() throws SnappydbException{
        if (fsDb == null ) {
            openDB() ;
        }
        if (fsDb.exists(FS_KEY)) {
            MasterFile mf = fsDb.getObject(FS_KEY, MasterFile.class);
            fsDb.close();
            return mf;
        }
        return null;
    }

    public synchronized void storeFileSystem() throws SnappydbException {
        MasterFile mf = retrieveFileSystem();
        if (null == mf) {
            fsDb.put(FS_KEY, MasterFile.getInstance());
        }else{
            fsDb.del(FS_KEY);
            fsDb.put(FS_KEY, MasterFile.getInstance());
        }
        fsDb.close();
    }

}
