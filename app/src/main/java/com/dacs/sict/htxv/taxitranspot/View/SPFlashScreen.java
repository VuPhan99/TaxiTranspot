package com.dacs.sict.htxv.taxitranspot.View;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.dacs.sict.htxv.taxitranspot.R;

public class SPFlashScreen extends AppCompatActivity {
    private int SP_FLASH_TIME_OUT = 2500;
    private ImageView imgSpFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spflash_screen);
        imgSpFlash = findViewById(R.id.imgSpFlashView);
        animateImage();
        setThread();
    }


        private void setThread() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(SPFlashScreen.this,LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        },SP_FLASH_TIME_OUT);



    }
    private void animateImage() {

        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(imgSpFlash,"x",1160f);
        objectAnimatorX.setDuration(2800);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimatorX);
        animatorSet.start();

    }

}
