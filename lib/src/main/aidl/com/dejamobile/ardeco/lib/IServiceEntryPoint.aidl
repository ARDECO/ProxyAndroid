// IServiceEntryPoint.aidl
package com.dejamobile.ardeco.lib;

import com.dejamobile.ardeco.lib.ArdecoCallBack;
import com.dejamobile.ardeco.lib.UserInfo;

// Declare any non-default types here with import statements

interface IServiceEntryPoint {

    String getVersion();

    void init(ArdecoCallBack callback);

    void createCommunity(in String id, in String signature, in ArdecoCallBack callback);

    void createService(in String communityId, in String serviceId, in String signature, in ArdecoCallBack callback);

    void readServiceContents(in String communityId, in String serviceId, in ArdecoCallBack callback);

    void readServiceTransactions(in String communityId, in String serviceId, in ArdecoCallBack callback);

    void updateUserInfo(in UserInfo userInfo, in ArdecoCallBack callback);

    void readUserInfo(in ArdecoCallBack callback);


}
