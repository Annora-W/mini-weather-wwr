package com.example.annora.weather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.pku.zhangqixun.bean.City;
import cn.pku.edu.wwr.App.MyApplication;

//选择城市界面的---Weather08
public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;//返回按钮

    /*ListView三种适配器的使用例子*/
    //level1
   /* private String[] data={"第1组","第2组","第3组","第4组","第5组","第6组","第7组","第8组",
            "第9组","第10组","第11组","第12组","第13组","第14组","第15组","第16组","第17组","第18组","第19组","第20组"};*/

    //level2
/*    private String[] name={"第1组","第2组","第3组","第4组"};
    private String[] desc={"田野、樊茂华、陈伟强、郭⼀娇、赵亚洪",
            "曹露阳、胡先军、彭俊伟、李粉英、段伟帝",
            "刘动、龚帅、王思可、赵林欣、陈佳佩",
            "魏红枪、张威、赵海洋、潘辉、袁金瑶"};
    private int[] imageids={R.drawable.base_action_bar_action_city, R.drawable.title_city,
            R.drawable.title_share,R.drawable.title_update};*/
    //level3
/*    String[] projection = null;
    String selection = null;
    String[] selectionArgs = null;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;//权限*/

    /*@RequiresApi(api = Build.VERSION_CODES.M)*/
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        /*ListView三种适配器的使用*/
        //--level1
        MyApplication mApplication = MyApplication.getInstance();
        final List<City> cityList=mApplication.getCityList();//获取城市列表
        final String[] viewList = new String[cityList.size()];//显示在ListView中的数据
        int i=0;
        for(City city:cityList){
            viewList[i]=city.getCity();
            i++;
        }
        ListView mlistView=(ListView)findViewById(R.id.list_view);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(//适配器
             SelectCity.this,
             android.R.layout.simple_list_item_1,
                viewList);//适配器
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//单击ListView的响应事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = cityList.get(position);
                Toast.makeText(SelectCity.this,"你单击了"+position+"："+city.getCity()+"，编码为"+city.getNumber(),Toast.LENGTH_SHORT).show();
            }
        });
        //--level2
        /*
        Map定义了访问特定集合的标准方法，这种集合用来存储key-value类型的键值对
        */
//        List<Map<String,Object>> listems=new ArrayList<Map<String, Object>>();//ListView的所有项
//        for(int i=0;i<name.length;i++)
//        {
//            Map<String,Object> listem=new HashMap<String, Object>();
//            listem.put("head",imageids[i]);//head、name、desc对应的是item.xml里的id名字
//            listem.put("name",name[i]);
//            listem.put("desc",desc[i]);
//            listems.add(listem);
//        }
//        SimpleAdapter simplead = new SimpleAdapter(
//               this,
//                listems,
//                R.layout.item,
//                new String[]{"name","head","desc"},
//                new int[]{R.id.name, R.id.head, R.id.desc});
//        mlistView.setAdapter(simplead);
        //--level3


        //https://blog.csdn.net/tiezhu_sun/article/details/49818915 还是无效？？？
//       int hasWriteContactsPermission=checkSelfPermission(Manifest.permission.WRITE_CONTACTS);
//        if(hasWriteContactsPermission!= PackageManager.PERMISSION_GRANTED){
//            requestPermissions(new String[]{
//                    Manifest.permission.WRITE_CONTACTS},
//                    REQUEST_CODE_ASK_PERMISSIONS);
//            return;
//        }
//        Cursor cursor=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                projection,selection,selectionArgs,null);
//        String[] fields=new String[]{
//                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                ContactsContract.CommonDataKinds.Phone.NUMBER
//        };
//        int[] toLayoutIDs=new int[]{android.R.id.text1,android.R.id.text2};
//        SimpleCursorAdapter myAdapter;
//        myAdapter=new SimpleCursorAdapter(
//                this,
//                android.R.layout.simple_list_item_2, //item的布局文件
//                cursor, //数据库游标
//                fields, //一个列名称列表，标志着绑定到视图的数据，如果游标不可用，则可为空
//                toLayoutIDs, //用来展示from数组中数据的视图，如果游标不可用，则可为空
//                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER); //用来确定适配器行为的标志
//        mlistView.setAdapter(myAdapter);
//
//        //返回按钮
//        mBackBtn = (ImageView)findViewById(R.id.title_back);
//        mBackBtn.setOnClickListener(this);
//
//        Log.d("SelectCity","SelectCity->oncreate");
    }

    //返回按钮的单击事件
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
