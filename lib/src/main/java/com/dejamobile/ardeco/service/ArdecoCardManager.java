package com.dejamobile.ardeco.service;

import android.os.RemoteException;
import android.util.Log;

import com.dejamobile.ConvertUtils;
import com.dejamobile.ardeco.card.APDU;
import com.dejamobile.ardeco.card.AbstractFile;
import com.dejamobile.ardeco.card.DedicatedFile;
import com.dejamobile.ardeco.card.ISOException;
import com.dejamobile.ardeco.card.MasterFile;
import com.dejamobile.ardeco.lib.ArdecoCallBack;
import com.dejamobile.ardeco.lib.Failure;

/**
 * Created by Sylvain on 18/06/2015.
 */
public class ArdecoCardManager {

    private static final String TAG = ArdecoCardManager.class.getCanonicalName();

    private static ArdecoCardManager instance;

    private ArdecoCardManager() {
    }

    public static ArdecoCardManager getInstance() {
        if (instance == null){
            instance = new ArdecoCardManager();
        }
        return instance;
    }

    public void createCommunity(String id, String signature, ArdecoCallBack callBack){
        String createDFApduStart = "00E000000E7800";
        String createDFApduEnd = "08004F44FF01031F11FF";
        String createDFApdu = createDFApduStart + id + createDFApduEnd;

        Log.d(TAG, "Community Creation with id : " + id);

        try {
            AbstractFile communityDF = ((DedicatedFile) MasterFile.getInstance()).createFile(new APDU(ConvertUtils.hex2byte(createDFApdu)));
            checkDFStatus(communityDF, callBack);
        }catch (ISOException e){
            try {
                callBack.onFailure(Failure.ILLEGAL_STATE);
            } catch (RemoteException re) {
                Log.w(TAG, re);
            }
        }
    }


    public void createService(short communityId, String servcieId, String signature, ArdecoCallBack callBack){
        String createDFApduStart = "00E000000E7800";
        String createDFApduEnd = "08004F44FF01031F11FF";
        String createDFApdu = createDFApduStart + servcieId + createDFApduEnd;

        // Community Exists ?
        DedicatedFile df = (DedicatedFile) ((DedicatedFile) MasterFile.getInstance()).getSibling(communityId);
        if (df != null) {
            try {
                AbstractFile serviceDF = df.createFile(new APDU(ConvertUtils.hex2byte(createDFApdu)));
                checkDFStatus(serviceDF, callBack);
            }catch (ISOException ie){
                try {
                    callBack.onFailure(Failure.ILLEGAL_STATE);
                } catch (RemoteException re) {
                    Log.w(TAG, re);
                }
            }
        }else{
            try {
                callBack.onFailure(Failure.FILE_NOT_FOUND);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
    }

    private void checkDFStatus( AbstractFile df, ArdecoCallBack callBack) {
        if (df == null){
            try {
                callBack.onFailure(Failure.NOT_ALLOWED_OPERATION);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }else{
            try {
                callBack.onSuccess();
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
    }

}
