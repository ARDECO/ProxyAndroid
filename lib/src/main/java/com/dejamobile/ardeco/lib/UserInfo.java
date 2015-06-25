package com.dejamobile.ardeco.lib;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sylvain on 11/06/2015.
 */
public class UserInfo implements Parcelable {

    private String ardecoId;
    private String preferredUsername;
    private String name;
    private String givenName;
    private String familyName;
    private String middleName;
    private String nickname;
    private String profile;
    private byte[] picture;
    private String website;
    private String email;
    private Boolean emailVerified;
    private String gender;
    private String zoneinfo;
    private String locale;
    private String phoneNumber;
    private Boolean phoneNumberVerified;
    private Address address;
    private String updatedTime;
    private String birthdate;

    private byte[] photo;

    public UserInfo() {
    }

    public String getArdecoId() {
        return ardecoId;
    }

    public void setArdecoId(String ardecoId) {
        this.ardecoId = ardecoId;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getZoneinfo() {
        return zoneinfo;
    }

    public void setZoneinfo(String zoneinfo) {
        this.zoneinfo = zoneinfo;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean isPhoneNumberVerified() {
        return phoneNumberVerified;
    }

    public void setPhoneNumberVerified(Boolean phoneNumberVerified) {
        this.phoneNumberVerified = phoneNumberVerified;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public UserInfo(Parcel in){
        ardecoId = in.readString();
        preferredUsername = in.readString();
        name = in.readString();
          givenName = in.readString();
          familyName = in.readString();
          middleName = in.readString();
          nickname = in.readString();
          profile = in.readString();
        in.readByteArray(picture);
         website = in.readString();
         email = in.readString();
          emailVerified = in.readByte() != 0;
          gender = in.readString();
          zoneinfo = in.readString();
          locale = in.readString();
          phoneNumber = in.readString();
          phoneNumberVerified = in.readByte() != 0;
          address = in.readParcelable(Address.class.getClassLoader());
          updatedTime = in.readString();
          birthdate = in.readString();

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(ardecoId);
        out.writeString(preferredUsername);
        out.writeString(name);
        out.writeString(givenName);
        out.writeString(familyName);
        out.writeString(middleName);
        out.writeString(nickname);
        out.writeString(profile);
        out.writeByteArray(picture);
        out.writeString(website);
        out.writeString(email);
        out.writeByte((byte) (emailVerified ? 1 : 0));
        out.writeString(gender);
        out.writeString(zoneinfo);
        out.writeString(locale);
        out.writeString(phoneNumber);
        out.writeByte((byte) (phoneNumberVerified ? 1 : 0));
        out.writeParcelable(address,i);
        out.writeString(updatedTime);
        out.writeString(birthdate);

    }

    public void readFromParcel(Parcel in){
        ardecoId = in.readString();
        preferredUsername = in.readString();
        name = in.readString();
        givenName = in.readString();
        familyName = in.readString();
        middleName = in.readString();
        nickname = in.readString();
        profile = in.readString();
        in.readByteArray(picture);
        website = in.readString();
        email = in.readString();
        emailVerified = in.readByte() != 0;
        gender = in.readString();
        zoneinfo = in.readString();
        locale = in.readString();
        phoneNumber = in.readString();
        phoneNumberVerified = in.readByte() != 0;
        address = in.readParcelable(Address.class.getClassLoader());
        updatedTime = in.readString();
        birthdate = in.readString();
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