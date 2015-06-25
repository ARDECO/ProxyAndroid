package com.dejamobile.ardeco_library;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dejamobile.ardeco.lib.Address;
import com.dejamobile.ardeco.lib.ArdecoCallBack;
import com.dejamobile.ardeco.lib.Failure;
import com.dejamobile.ardeco.lib.IServiceEntryPoint;
import com.dejamobile.ardeco.lib.ServiceEntryPoint;
import com.dejamobile.ardeco.lib.UserInfo;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = BuildConfig.LOG_ADB_KEY;

    private static IServiceEntryPoint entryPoint;

    private TextView tvVersion;

    private EditText editTextName;

    private EditText editTextEmail;

    Spinner spinnerCommIds;

    Spinner spinnerServiceIds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvVersion = (TextView) findViewById(R.id.tvVersion);

        editTextName = (EditText) findViewById(R.id.editTextName);

        editTextEmail = (EditText) findViewById( R.id.editTextEmail);

        spinnerCommIds = (Spinner) findViewById(R.id.spinnerCommunityId);
        ArrayAdapter<CharSequence> commAdapter = ArrayAdapter.createFromResource(this,
                R.array.comm_ids_array, android.R.layout.simple_spinner_item);
        commAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommIds.setAdapter(commAdapter);

        spinnerServiceIds = (Spinner) findViewById(R.id.spinnerServiceId);
        ArrayAdapter<CharSequence> serviceAdapter = ArrayAdapter.createFromResource(this,
                R.array.service_ids_array, android.R.layout.simple_spinner_item);
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServiceIds.setAdapter(serviceAdapter);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, ServiceEntryPoint.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            entryPoint = IServiceEntryPoint.Stub.asInterface(service);
            String libraryVersion;
            try {
                // Library Call is here for demo purpose
                libraryVersion = entryPoint.getVersion();
                tvVersion.setText(libraryVersion);

                Log.d(TAG, "Library version is : " + libraryVersion);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Service has unexpectedly disconnected");
            entryPoint = null;
        }
    };

    public void onClickBtnUpdateInfo(View v){
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        UserInfo userInfo = new UserInfo();
        Address address = new Address();
        address.setStreetAddress("Rue de l'impasse");
        address.setPostalCode("14400");
        address.setLocality("CAEN");
        userInfo.setAddress(address);
        userInfo.setName(name);
        userInfo.setEmail(email);
        userInfo.setArdecoId("0123456789abcdef");
        try {
            entryPoint.updateUserInfo(userInfo, new ArdecoCallBack() {
                @Override
                public void onSuccess() throws RemoteException {
                    Log.d(TAG, "User Info has been successfully updated");
                }

                @Override
                public void onFailure(Failure failure) throws RemoteException {
                    Log.w(TAG, "User Info update has failed with reason : " + failure.name());
                }

                @Override
                public void onUserInfoRead(UserInfo userInfo) throws RemoteException {
                }

                @Override
                public IBinder asBinder() {
                    return null;
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void onClickBtnReadInfo(View v){

        try {
            entryPoint.readUserInfo(new ArdecoCallBack() {
                @Override
                public void onSuccess() throws RemoteException {
                    Log.d(TAG, "User Info has been successfully Read");
                }

                @Override
                public void onFailure(Failure failure) throws RemoteException {
                    Log.w(TAG, "User Info retrieval has failed with reason : " + failure.name());
                }

                @Override
                public void onUserInfoRead(UserInfo userInfo) throws RemoteException {
                    Log.d(TAG, "User Info has been retrieved");
                    Log.d(TAG, "User : " + userInfo.getName());
                    Log.d(TAG, "Email : " + userInfo.getEmail());
                    Log.d(TAG, "Id : " + userInfo.getArdecoId());
                }

                @Override
                public IBinder asBinder() {
                    return null;
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    public void onClickBtnCreateCommunity(View v){
        final String communityId = (String)spinnerCommIds.getSelectedItem();

        try {
            entryPoint.createCommunity(communityId, null, new ArdecoCallBack() {
                @Override
                public void onSuccess() throws RemoteException {
                    Log.d(TAG, "Community : " + communityId + " has been successfully created");
                }

                @Override
                public void onFailure(Failure failure) throws RemoteException {
                    Log.w(TAG, "Community : " + communityId + " has not been created. " + failure.name());
                }

                @Override
                public void onUserInfoRead(UserInfo userInfo) throws RemoteException {

                }

                @Override
                public IBinder asBinder() {
                    return null;
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onClickBtnCreateService(View v){
        final String communityId = (String)spinnerCommIds.getSelectedItem();
        final String serviceId = (String)spinnerServiceIds.getSelectedItem();

        try {
            entryPoint.createService(communityId, serviceId, null, new ArdecoCallBack() {
                @Override
                public void onSuccess() throws RemoteException {
                    Log.d(TAG, "Service : " + serviceId + " has been successfully created");
                }

                @Override
                public void onFailure(Failure failure) throws RemoteException {
                    Log.w(TAG, "Service : " + serviceId + " has not been created. " + failure.name());
                }

                @Override
                public void onUserInfoRead(UserInfo userInfo) throws RemoteException {

                }

                @Override
                public IBinder asBinder() {
                    return null;
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
