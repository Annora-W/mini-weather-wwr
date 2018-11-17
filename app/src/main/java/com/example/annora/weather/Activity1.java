package com.example.annora.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Activity1 extends Activity {

    private Button btn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Activity1", "onCreate");
        setContentView(R.layout.activity1);
        btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Activity1.this, Activity2.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Activity1", "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Activity1", "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Activity1", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Activity1", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Activity1", "onDestroy");
    }
}
