package com.example.annora.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

//选择城市界面的---Weather08
public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;//返回按钮

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        Log.d("SelectCity","SelectCity->oncreate");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_back:
                Intent i=new Intent();//weather08-2
                i.putExtra("cityCode","101160101");//weather08-2
                setResult(RESULT_OK, i);//weather08-2
                finish();
                break;
            default:
                break;
        }
    }
}
