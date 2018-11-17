package com.example.annora.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Map;

import cn.pku.edu.wwr.Adapter.SearchCityAdapter;
import cn.pku.edu.wwr.bean.City;
import cn.pku.edu.wwr.App.MyApplication;
import cn.pku.edu.wwr.db.CityDB;
import cn.pku.edu.wwr.util.SharedPreferenceUtil;
import javaLayout.BladeView;
import javaLayout.BladeView.OnItemClickListener;

//选择城市界面的---Weather08
public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;//返回按钮
    private String cityCode; //返回到城市编码
    private TextView titleName;//顶部标题的文字控件
    private EditText mSearchEditText;//搜索框
    private SearchCityAdapter mSearchCityAdapter;//搜索城市的适配器
    private ArrayAdapter<String> cityAdapter;//所有城市的适配器
    private List<City> mCityList;//城市列表
    private ListView mCityListView;//城市管理界面的ListView
    private SharedPreferenceUtil mSpUtil;
    private MyApplication mApplication;
    private HashMap<String, City> cityCode_cityHashMap;

    private BladeView mLetter;//首字母？？
    // 首字母对应的位置
    private Map<String, Integer> mIndexer;
    // 根据首字母存放数据
    private Map<String, List<City>> mMap;

    //使用TextWatcher监听EditText变化
    TextWatcher mTextWatcher = new TextWatcher() {
        //变化前
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        //变化中
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSearchCityAdapter = new SearchCityAdapter(SelectCity.this, mCityList);//根据输入的内容创建适配器

            mCityListView.setTextFilterEnabled(true);//数据过滤
            if(mCityList.size() < 1 || TextUtils.isEmpty(s)){//输入为空或者城市列表为空
                mCityListView.setAdapter(cityAdapter);
                //mCityContainer.setVisibility(View.VISIBLE);//显示原始城市列表 View.VISIBLE--->可见
                //mSearchContainer.setVisibility(View.INVISIBLE);//View.INVISIBLE--->不可见，但这个View仍然会占用在xml文件中所分配的布局空间，不重新layout
                //mClearSearchBtn.setVisibility(View.GONE);//不可见，但这个View在ViewGroup中不保留位置，会重新layout，不再占用空间，那后面的view就会取代他的位置，
            }else{
                mCityListView.setAdapter(mSearchCityAdapter);//重置ListView的适配器
                //mClearSearchBtn.setVisibility(View.VISIBLE);//显示清除输入按钮
                //mCityContainer.setVisibility(View.INVISIBLE);
                //mSearchContainer.setVisibility(View.VISIBLE);//显示查找的城市列表
                mSearchCityAdapter.getFilter().filter(s);//数据的过滤以及刷新展示
            }
        }
        //变化后
        @Override
        public void afterTextChanged(Editable s) {

        }
    };
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

        initData();//初始化数据
        //更新ListView
        updateListView();
        InitView();//更新顶部当前城市文字

        //返回按钮
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        Log.d("SelectCity","SelectCity->oncreate");
    }

    //初始化数据
    private void initData(){
        mApplication = MyApplication.getInstance();
        mCityList = mApplication.getCityList();//获取城市列表
        mSpUtil = mApplication.getSharedPreferenceUtil();
        cityCode_cityHashMap = new HashMap<>();

        mIndexer = mApplication.getIndexer();///@
        mMap = mApplication.getMap();

        for(City city : mCityList){
            cityCode_cityHashMap.put(city.getNumber(), city);
        }
    }

    //返回按钮的单击事件
    /*  setResult(int resultCode, Intent data)
　　在意图跳转的目的地界面调用这个方法把Activity想要返回的数据返回到主Activity，
    第一个参数：当Activity结束时resultCode将归还在onActivityResult()中，一般为RESULT_CANCELED , RESULT_OK该值默认为-1。
    第二个参数：一个Intent对象，返回给主Activity的数据。在intent对象携带了要返回的数据，使用putExtra( )方法。
    */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //点击返回按钮
            case R.id.title_back:
                Log.d("myWeather","click back");
                Intent i=new Intent();//weather08-2
                //i.putExtra("cityCode","101160101");
                i.putExtra("cityCode",cityCode);//putExtra将计算的值回传回去；返回城市编号--weather08-2
                setResult(RESULT_OK, i);//weather08-2
                finish();//结束当前的activity的生命周期
                break;
            default:
                break;
        }
    }

    //用适配器更新ListView
    void updateListView()
    {
        /*ListView三种适配器的使用*/
        //--level1
        final String[] viewList = new String[mCityList.size()];//显示在ListView中的数据
        int i=0;
        for(City city:mCityList){
            viewList[i]=city.getCity();
            i++;
        }
        mCityListView = (ListView)findViewById(R.id.list_view);
        cityAdapter = new ArrayAdapter<String>(//适配器
                SelectCity.this,
                android.R.layout.simple_list_item_1,
                viewList);//适配器
        mCityListView.setAdapter(cityAdapter);
        //ListView适配器的单击响应事件
        mCityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//单击ListView的响应事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city;
                if(mSearchCityAdapter != null){
                    city = /*mCityList.get(position);*/(City)mSearchCityAdapter.getItem(position);
                }
                else
                    city = mCityList.get(position);
                cityCode = city.getNumber();
                updateTitleName(city.getCity());
                Toast.makeText(SelectCity.this,"你单击了"+position+"："+ city.getCity() +"，编码为"+ cityCode,Toast.LENGTH_SHORT).show();
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
    }

    //初始化UI控件
    void InitView(){
        //将控件名与id关联
        titleName=(TextView)findViewById(R.id.title_name);//标题名
        mSearchEditText=(EditText)findViewById(R.id.search_edit);//搜索框
        mSearchEditText.addTextChangedListener(mTextWatcher);

        //初始化控件内容
        //titleName.setText("当前城市：北京");
        City curCity = cityCode_cityHashMap.get(mSpUtil.getCurCityCode());//根据当前城市编码得到城市名
        titleName.setText("当前城市：" + curCity.getCity());

        //
        mLetter = (BladeView) findViewById(R.id.citys_bladeview);
        //右侧首字母的点击事件
        mLetter.setOnItemClickListener(new OnItemClickListener(){//点击右侧字母响应事件
            @Override
            public void onItemClick(String s) {
                if (mIndexer.get(s) != null) {
                    mCityListView.setSelection(mIndexer.get(s));//把第mIndexer.get(s)个item显示在ListView的最上方
                }
                Log.d("Letter","点击首字母");
            }
        });
        //mLetter.setVisibility(View.GONE);
    }

    //更新顶部当前城市的文字
    void updateTitleName(String cityName){
        titleName.setText("当前城市：" + cityName);
    }
}
