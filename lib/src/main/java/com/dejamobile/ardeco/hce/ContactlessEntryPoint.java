package com.dejamobile.ardeco.hce;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.dejamobile.ConvertUtils;
import com.dejamobile.ardeco.card.APDU;
import com.dejamobile.ardeco.card.ArdecoApplet;
import com.dejamobile.ardeco.card.ISOException;
import com.dejamobile.ardeco.util.DBManager;
import com.snappydb.SnappydbException;


/**
 * Created by 6l20 on 24/07/2014.
 */
public class ContactlessEntryPoint extends HostApduService {

    private static final String TAG = ContactlessEntryPoint.class.getName();

    private static final String TRACE_TAG = "trace_process_command_apdu_";

    private ArdecoApplet ardecoApplet = new ArdecoApplet();

    @Override
    public void onCreate() {
    }

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle bundle) {

        Log.d(TAG,"Received APDU : " + ConvertUtils.toHexString(apdu));
        APDU processedApdu = new APDU(apdu);

        try {

            ardecoApplet.processApdu(processedApdu);
        }catch(ISOException ie){
            Log.d(TAG,"ISO Exception : " + ie.getCode());
            return ie.getAsByteArray();
        }catch(Throwable t){
            Log.e(TAG,"Throwable." + t.getMessage());
            return new byte[]{0x6f,0};
        }
        return processedApdu.getResponse();


    }

    @Override
    public void onDeactivated(int i) {
        try {
            Log.d(TAG,"File System persist");
            DBManager.getInstance(getApplicationContext()).storeFileSystem();
        } catch (SnappydbException e) {
            Log.w(TAG, "File System persist issue " + e.getMessage());
        }
    }
}
