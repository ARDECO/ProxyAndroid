package com.dejamobile.ardeco.lib;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sylvain on 11/06/2015.
 */
public class UserInfo implements Parcelable {


    private String mailAddress;

    private String firstName;

    private String lastName;

    private String birthDate;

    public UserInfo(String mailAddress, String firstName, String lastName, String birthDate) {
        this.mailAddress = mailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public UserInfo(Parcel in){
        mailAddress = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        birthDate = in.readString();
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(mailAddress);
        out.writeString(firstName);
        out.writeString(lastName);
        out.writeString(birthDate);
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(final Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(final int size) {
            return new UserInfo[size];
        }
    };
}