// ArdecoCallBack.aidl
package com.dejamobile.ardeco.lib;

import com.dejamobile.ardeco.lib.Failure;

// Declare any non-default types here with import statements

interface ArdecoCallBack {

    void onSuccess();

    void onFailure(in Failure failure);
    
}
