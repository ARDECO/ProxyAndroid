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
import android.util.Log;

import com.dejamobile.ardeco.card.MasterFile;
import com.dejamobile.ardeco.service.ArdecoCardManager;
import com.dejamobile.ardeco.util.DBManager;
import com.snappydb.SnappydbException;

/**
 * Created by Sylvain on 21/04/2015.
 */
public class ServiceEntryPoint extends Service {

    private static final String TAG = ServiceEntryPoint.class.getName();

    protected final DelegateEntryPoint mBinder = new DelegateEntryPoint();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding Service");

        return mBinder.asBinder();
    }


    protected class DelegateEntryPoint extends IServiceEntryPoint.Stub {

        @Override
        public String getVersion() throws RemoteException {
            return BuildConfig.VERSION_NAME;
        }

        public void init(ArdecoCallBack callback){
            checkCallback(callback);

        }

        public void createCommunity(String id, String signature, ArdecoCallBack callback){
            checkCallback(callback);
            ArdecoCardManager.getInstance().createCommunity(id, signature, callback);
        }

        public void createService(String communityId, String serviceId, String signature, ArdecoCallBack callback){
            checkCallback(callback);
            ArdecoCardManager.getInstance().createService((short) Integer.parseInt(communityId, 16), serviceId, signature, callback);
        }

        public void readServiceContents(String communityId, String serviceId, ArdecoCallBack callback){
            checkCallback(callback);
        }

        public void readServiceTransactions(String communityId, String serviceId, ArdecoCallBack callback){
            checkCallback(callback);
        }

        public void updateUserInfo(UserInfo userInfo, ArdecoCallBack callback){
            checkCallback(callback);
            Log.d(TAG, "UserInfo name : " + userInfo.getName() + " " + userInfo.getFamilyName());
            Log.d(TAG, "UserInfo address : " + userInfo.getAddress().getLocality() + " " + userInfo.getAddress().getPostalCode());

            try {
                DBManager.getInstance(getApplicationContext()).storeUserInfo(userInfo);
            } catch (SnappydbException e) {
                Log.w(TAG, "Something went wrong while storing UserInfo " + e.getMessage(), e);
                try {
                    callback.onFailure(Failure.UNKNOWN);
                } catch (RemoteException e1) {
                    Log.w(TAG, "Something went wrong while invoking callBack " + e1.getMessage(), e1);
                }
            }
            try {
                callback.onSuccess();
            } catch (RemoteException e) {
                Log.w(TAG, "Something went wrong while invoking callBack " + e.getMessage(), e);
            }
        }


        public void readUserInfo(ArdecoCallBack callback){
            checkCallback(callback);

            try {
                UserInfo retrievedUserInfo = DBManager.getInstance(getApplicationContext()).retrieveUserInfo();
                try {
                    callback.onUserInfoRead(retrievedUserInfo);
                } catch (RemoteException e) {
                    Log.w(TAG, "Something went wrong while invoking callBack " + e.getMessage(), e);
                }
            } catch (SnappydbException e) {
                Log.w(TAG, "Something went wrong while reading info " + e.getMessage(), e);
            }

        }






    }

    private void checkCallback(ArdecoCallBack callback) {
        if (callback == null){
            throw new IllegalArgumentException("callback parameter must not be null");
        }
    }
}
