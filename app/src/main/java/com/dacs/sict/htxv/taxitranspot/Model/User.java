package com.dacs.sict.htxv.taxitranspot.Model;

public class User {
    private String mName, mEmail, mPhoneNumber, mPassword;

    public User() {
    }

    public User(String mName, String mEmail, String mPhoneNumber, String mPassword) {
        this.mName = mName;
        this.mEmail = mEmail;
        this.mPhoneNumber = mPhoneNumber;
        this.mPassword = mPassword;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmPhoneNumber() {
        return mPhoneNumber;
    }

    public void setmPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public String getmPassword() {
        return mPassword;
    }

    public void setmPassword(String mPassword) {
        this.mPassword = mPassword;
    }
}
