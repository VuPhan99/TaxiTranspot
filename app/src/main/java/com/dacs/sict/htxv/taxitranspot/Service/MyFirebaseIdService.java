package com.dacs.sict.htxv.taxitranspot.Service;

import com.dacs.sict.htxv.taxitranspot.Common.Common;
import com.dacs.sict.htxv.taxitranspot.Model.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseIdService extends FirebaseMessagingService {
    //press ctrl + o


    @Override
    public void onNewToken(String s) {
        super.onNewToken( s );
        updateTokenToServer( s );//when have refres token, we need update to our realtime database
    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference( Common.token_tbl );

        Token token = new Token( refreshedToken );

        if (FirebaseAuth.getInstance().getCurrentUser() != null)//if already login, must update token
            tokens.child( FirebaseAuth.getInstance().getCurrentUser().getUid() ).setValue( token );
    }
}
