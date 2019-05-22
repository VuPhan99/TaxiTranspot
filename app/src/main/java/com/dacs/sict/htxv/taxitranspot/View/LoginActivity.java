package com.dacs.sict.htxv.taxitranspot.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dacs.sict.htxv.taxitranspot.Common.Common;
import com.dacs.sict.htxv.taxitranspot.Common.SessionManager;
import com.dacs.sict.htxv.taxitranspot.Model.User;
import com.dacs.sict.htxv.taxitranspot.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnSignIn;
    private Button btnSignUp;
    private MaterialEditText edtEmail;
    private MaterialEditText edtName;
    private MaterialEditText edtPhoneNumber;
    private MaterialEditText edtPassword;

    private View signUpLayout;
    private View signInLayout;
    LinearLayout rootLayout;



    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try{
            mSessionManager = new SessionManager(this);
            String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.e("dacs_", "In try catch ne1" + uID);
            if(mSessionManager.isLogin() && mSessionManager.checkUserId().equals(uID)){
                Log.e("dacs_", "In try catch ne");
                Intent intent = new Intent(this,Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        }catch (NullPointerException e){
            Log.e("dacs_", String.valueOf(e));
        }


        //Add Controlls
        addControls();

        //Init Database Firebase
        initFirebase();


    }


    private void addControls() {
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);

        //Mapping
        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);


//        View register_layout = layoutInflater.inflate(R.layout.activity_signup_dialog,null);
//
        LayoutInflater layoutInflater = LayoutInflater.from(LoginActivity.this);
        signUpLayout = layoutInflater.inflate(R.layout.activity_signup_dialog,null,false);
        signInLayout = layoutInflater.inflate(R.layout.activity_signin_dialog,null,false);




    }

    private void initFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference(Common.user_driver_tbl );

    }


    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        if(button.getId() == R.id.btnSignIn){
            showDialogSignIn();
        }
        if(button.getId() == R.id.btnSignUp){
            showDialogSignUp();
        }
    }

    private void showDialogSignUp() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
        dialog.setTitle(R.string.login_title_dialog_signup);
        dialog.setMessage(R.string.login_text_dialog_mess_signup);

        //Checked parent
        ViewParent viewParent = signUpLayout.getParent();
        if (viewParent != null) {
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(signUpLayout);
            }
        }

        dialog.setView(signUpLayout).create();

        edtEmail = signUpLayout.findViewById(R.id.edtEmailSignUp);
        edtName = signUpLayout.findViewById(R.id.cclmm);
        edtPhoneNumber = signUpLayout.findViewById(R.id.edtPhoneSignUp);
        edtPassword = signUpLayout.findViewById(R.id.edtPasswordSignUp);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootLayout,R.string.login_miss_email,Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtName.getText().toString())){
                    Snackbar.make(rootLayout,R.string.login_miss_name,Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPhoneNumber.getText().toString())){
                    Snackbar.make(rootLayout,R.string.login_miss_phone,Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())){
                    Snackbar.make(rootLayout,R.string.login_miss_password,Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().toString().length() < 6){
                    Snackbar.make(rootLayout,R.string.login_miss_password_length,Snackbar.LENGTH_SHORT).show();
                    return;
                }

                mFirebaseAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        User user = new User();
                        user.setmEmail(edtEmail.getText().toString());
                        user.setmName(edtName.getText().toString());
                        user.setmPhoneNumber(edtPhoneNumber.getText().toString());
                        user.setmPassword(edtPassword.getText().toString());

                        //user Email to key
                        mDatabaseReference.child(
                                FirebaseAuth.getInstance().getCurrentUser().getUid()
                        ).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(rootLayout,R.string.login_text_register_success,Snackbar.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,R.string.login_text_register_fail,Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout,R.string.login_text_register_fail,Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create().show();
    }

    private void showDialogSignIn() {


        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
        dialog.setTitle(R.string.login_title_dialog_signup);
        dialog.setMessage(R.string.login_text_dialog_mess_signin);
//
//        LayoutInflater layoutInflater = LayoutInflater.from(LoginActivity.this);
//        View signInLayout = layoutInflater.inflate(R.layout.activity_signin_dialog,null);

        edtEmail = signInLayout.findViewById(R.id.edtEmailSignIn);
        edtPassword = signInLayout.findViewById(R.id.edtPasswordSignIn);

        ViewParent viewParent = signInLayout.getParent();
        if (viewParent != null) {
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(signInLayout);
            }
        }
        dialog.setView(signInLayout);


        dialog.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(LoginActivity.this);

                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootLayout,R.string.login_miss_email,Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())){
                    edtPassword.setText("");
                    Snackbar.make(rootLayout,R.string.login_miss_password,Snackbar.LENGTH_SHORT).show();
                    return;
                }
                waitingDialog.show();
                mFirebaseAuth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //success
                        mSessionManager.SetLogin(true,mFirebaseAuth.getCurrentUser().getUid());

                        waitingDialog.dismiss();
                        Intent intent = new Intent(LoginActivity.this,Home.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout,R.string.login_text_signin_fail,Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
