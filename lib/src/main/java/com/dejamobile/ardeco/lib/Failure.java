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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sylvain.
 */
public enum Failure implements Parcelable {

    UNSUPPORTED_DEVICE,

    UNAVAILABLE_NETWORK,
    INVALID_RESPONSE,

    TIMEOUT,
    AUTHENTICATION_ERROR,
    UNAUTHORIZED_OPERATION,

    INVALID_REQUEST,
    NOT_ALLOWED_OPERATION,
    NO_CARD_AVAILABLE,
    ILLEGAL_ARGUMENT,
    ILLEGAL_STATE,
    UNKNOWN,
    //APDU ERRORS
    CLA_INVALID,
    P1_OR_P2_INVALID,
    LC_INVALID,
    WRONG_LENGTH, FILE_NOT_FOUND;





    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name());
    }

    public static final Creator<Failure> CREATOR = new Creator<Failure>() {
        @Override
        public Failure createFromParcel(final Parcel source) {
            return Failure.valueOf(source.readString());
        }

        @Override
        public Failure[] newArray(final int size) {
            return new Failure[size];
        }
    };

}
