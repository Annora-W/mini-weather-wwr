package com.example.annora.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Activity2 extends Activity {
    private Button btn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Activity2", "onCreate");
        setContentView(R.layout.activity2);
        btn = (Button)findViewById(R.id.btn2);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(Activity1.this, Activity2.class);
                //startActivity(i);
                finish();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Activity2", "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Activity2", "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Activity2", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Activity2", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Activity2", "onDestroy");
    }
}
