package com.dacs.sict.htxv.taxitranspot.Common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CheckLoggedIn {
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    //    private DatabaseReference mDatabaseReference;
//    private FirebaseDatabase  mFirebaseDatabase;
    private boolean checkLogged;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public CheckLoggedIn(Context context) {
//        try{
//            setCheckLogged(true);
////            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//             mFirebaseAuth.getInstance().getCurrentUser().getUid();
//            String s = mFirebaseUser.getUid();
////            Log.e("theUser",FirebaseAuth.getInstance().getCurrentUser());
//            Log.e("theUser","Checlogged.class: "+mFirebaseAuth.getInstance().getCurrentUser().getUid());
//            Log.e("checklogin_boolean",String.valueOf(checkLogged));
//        }
//        catch (NullPointerException e){
//            setCheckLogged(false);
//            Log.e("checklogin_boolean",String.valueOf(checkLogged));
//
//
//            return;
//        }

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    setCheckLogged(true);
                    Log.e("theUser", checkLogged + user.getEmail());
                }
                else{
                    setCheckLogged(false);
                    Log.e("theUser", checkLogged +"loggedfalse");
                }
            }
        };
    }

    public void setCheckLogged(boolean checkLogged) {
        this.checkLogged = checkLogged;
    }

    public boolean isLogged() {
        return checkLogged;
    }


}

