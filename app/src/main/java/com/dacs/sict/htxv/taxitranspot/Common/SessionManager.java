package com.dacs.sict.htxv.taxitranspot.Common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

public class SessionManager {
    private static String TAG = SessionManager.class.getName();
    SharedPreferences preferences;
    Context context;
    SharedPreferences.Editor editor;
    private int MODE_PRIVATE = 1;
    private static final String NAME = "firebase_login";
    private static final String KEY_LOGIN = "islogin";
    private static final String KEY_ID_USER = "IDUser";

    private FirebaseAuth mFirebaseAuth;

    @SuppressLint("WrongConstant")
    public SessionManager(Context context){
        this.context = context;
        preferences = context.getSharedPreferences(NAME,context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    public void SetLogin(boolean isLogin, String uId){
        editor.putString(KEY_ID_USER,uId);
        editor.putBoolean(KEY_LOGIN,isLogin);
        editor.commit();
    }
    public boolean isLogin(){
        return preferences.getBoolean(KEY_LOGIN,false);
    }

    public String checkUserId(){
        return preferences.getString(KEY_ID_USER,null);
    }


}
