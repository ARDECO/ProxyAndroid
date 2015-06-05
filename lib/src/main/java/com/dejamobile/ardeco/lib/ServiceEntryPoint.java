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
package com.dejamobile.ardeco.lib;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.dejamobile.ardeco.card.MasterFile;
import com.dejamobile.ardeco.util.DBManager;
import com.snappydb.SnappydbException;

/**
 * Created by Sylvain on 21/04/2015.
 */
public class ServiceEntryPoint extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        try {
            DBManager.getInstance(getApplicationContext()).openDB();
            MasterFile mf = DBManager.getInstance(getApplicationContext()).retrieveFileSystem();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            DBManager.getInstance(getApplicationContext()).openDB();
            DBManager.getInstance(getApplicationContext()).storeFileSystem();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return super.onUnbind(intent);

    }

    protected class DelegateEntryPoint extends IServiceEntryPoint.Stub {

        @Override
        public String getVersion() throws RemoteException {
            return BuildConfig.VERSION_NAME;
        }

        public void init(ArdecoCallBack callback){

        }

        public void createCommunity(String id, String signature, ArdecoCallBack callback){

        }

        public void createService(String communityId, String serviceId, String signature, ArdecoCallBack callback){

        }

        public void readServiceContents(String communityId, String serviceId, ArdecoCallBack callback){

        }

        public void readServiceTransactions(String communityId, String serviceId, ArdecoCallBack callback){

        }




    }
}
