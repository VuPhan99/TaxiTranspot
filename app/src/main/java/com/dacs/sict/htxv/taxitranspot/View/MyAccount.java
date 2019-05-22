package com.dacs.sict.htxv.taxitranspot.View;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dacs.sict.htxv.taxitranspot.Common.Common;
import com.dacs.sict.htxv.taxitranspot.Common.SessionManager;
import com.dacs.sict.htxv.taxitranspot.Model.InformationUser;
import com.dacs.sict.htxv.taxitranspot.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.io.FileUtils;

import dmax.dialog.SpotsDialog;

public class MyAccount extends AppCompatActivity implements View.OnClickListener {

    private TextView txtName, txtNotifications;
    private EditText edtName, edtPhoneNumber, edtEmail;
    private Button btnBack, btnSave, btnGoogleLink, btnLogout;
    private ImageView imgNotifications;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private TextWatcher textWatcher;
    private AlertDialog mWaitingDialog;

    private boolean checkEditTextChanged;
    private SessionManager mSessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        checkLoggedIn();
        addControls();
        setInformation();
    }

    private void checkLoggedIn() {
        try{
            mSessionManager = new SessionManager(getApplicationContext());
            String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if(!mSessionManager.isLogin() && mSessionManager.checkUserId() != uID){
                Toast.makeText(this, "Please login to countinue", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                this.startActivity(intent);
            }
        }catch (NullPointerException e){}
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        switch (button.getId()){
            case R.id.btn_myaccount_back:
                finish();
                break;
            case R.id.btn_myaccount_save:
                updateInformation();
                break;
            case R.id.btn_myaccount_google_account_link:
                googleLink();
                break;
            case R.id.btn_myaccount_logout:
                logout();
        }
    }

    private void addControls() {
        txtName = findViewById(R.id.txt_myaccount_name);
        txtNotifications = findViewById(R.id.txt_myaccount_notifications);
        edtName = findViewById(R.id.edt_myaccount_name);
        edtPhoneNumber = findViewById(R.id.edt_myaccount_phone);
        edtEmail = findViewById(R.id.edt_myaccount_email);
        btnBack = findViewById(R.id.btn_myaccount_back);
        btnSave = findViewById(R.id.btn_myaccount_save);
        btnGoogleLink = findViewById(R.id.btn_myaccount_google_account_link);
        btnLogout = findViewById(R.id.btn_myaccount_logout);
        imgNotifications = findViewById(R.id.img_myaccount_notifications);


        //Set Onclick event for button
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnGoogleLink.setOnClickListener(this);
        btnLogout.setOnClickListener(this);


    }

    private void setInformation() {
        mWaitingDialog = new SpotsDialog(MyAccount.this,"Getting Information");
        mWaitingDialog.show();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference( Common.user_driver_tbl ).child(mFirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                InformationUser uInfo = dataSnapshot.getValue(InformationUser.class);

                //Set information to Text   View, Edittext
                txtName.setText(uInfo.getmName());
                edtName.setText(uInfo.getmName());
                edtPhoneNumber.setText(uInfo.getmPhoneNumber());
                edtEmail.setText(uInfo.getmEmail());

                mWaitingDialog.dismiss();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setAlphaAnimation() {
        // fade out view nicely after 5 seconds

        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f,0.0f);
        alphaAnim.setStartOffset(5000);                        // start in 5 seconds
        alphaAnim.setDuration(400);
        alphaAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation)
            {
                // make invisible when animation completes, you could also remove the view from the layout
                txtNotifications.setVisibility(View.INVISIBLE);
                imgNotifications.setImageResource(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        txtNotifications.setAnimation(alphaAnim);
        imgNotifications.setAnimation(alphaAnim);
    }

    private void checkEditTextChanged() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                checkEditTextChanged = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkEditTextChanged = true;
            }
        };

        edtName.addTextChangedListener(textWatcher);
        edtPhoneNumber.addTextChangedListener(textWatcher);
        edtEmail.addTextChangedListener(textWatcher);
    }



    private void updateInformation() {
        checkEditTextChanged();
        if(checkEditTextChanged == false){
            txtNotifications.setText(R.string.myaccount_notifications_onchange);
            imgNotifications.setImageResource(R.drawable.ic_deny);
            setAlphaAnimation();
        }
        else{
            mWaitingDialog = new SpotsDialog(MyAccount.this,"Saving");
            mWaitingDialog.show();

            //Get information
            final InformationUser informationUser = new InformationUser();
            informationUser.setmName(edtName.getText().toString());
            informationUser.setmEmail(edtEmail.getText().toString());
            informationUser.setmPhoneNumber(edtPhoneNumber.getText().toString());

            mDatabaseReference.setValue(informationUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    checkEditTextChanged = false;
                    mWaitingDialog.dismiss();
                    txtNotifications.setText(R.string.myaccount_notifications_success);
                    imgNotifications.setImageResource(R.drawable.ic_checked);
                    setAlphaAnimation();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mWaitingDialog.dismiss();
                    Toast.makeText(MyAccount.this, "Failed" + e, Toast.LENGTH_SHORT).show();

                }
            });
        }

    }

    private void googleLink() {
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void logout() {
        mFirebaseAuth.getInstance().signOut();
        FileUtils.deleteQuietly(getApplicationContext().getCacheDir());
        FileUtils.deleteQuietly(getApplicationContext().getExternalCacheDir());
        FileUtils.deleteQuietly(getApplicationContext().getCodeCacheDir());
        FileUtils.deleteQuietly(getApplicationContext().getDataDir());
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
