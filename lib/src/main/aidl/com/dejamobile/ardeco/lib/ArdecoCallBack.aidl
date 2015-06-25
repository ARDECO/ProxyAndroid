// ArdecoCallBack.aidl
package com.dejamobile.ardeco.lib;

import com.dejamobile.ardeco.lib.Failure;

import com.dejamobile.ardeco.lib.UserInfo;

// Declare any non-default types here with import statements

interface ArdecoCallBack {

    void onSuccess();

    void onFailure(in Failure failure);

    void onUserInfoRead(out UserInfo userInfo);
    
}
