package com.example.annora.weather;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.pku.edu.wwr.Adapter.ViewPagerAdapter;
import cn.pku.edu.wwr.App.MyApplication;
import cn.pku.edu.wwr.bean.City;
import cn.pku.edu.wwr.bean.TodayWeather;
import cn.pku.edu.wwr.bean.WeekWeather;
import cn.pku.edu.wwr.db.CityDB;
import cn.pku.edu.wwr.util.NetUtil;
import cn.pku.edu.wwr.util.SharedPreferenceUtil;


/*implements View.OnClickListener添加按钮单击事件
implements是一个类，实现一个接口用的关键字，它是用来实现接口中定义的抽象方法。*/
public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener{ //项目中所有活动必须继承Activity或它的子类才能拥有活动的特性

    //按钮
    private ImageView mUpdateBtn;//刷新按钮---weather05
    private ImageView mCitySelect;//左上方，选择城市按钮---weather08
    private ProgressBar mUpdateProgressBar;//刷新按钮动画
    //文字控件、图片控件
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv,
            pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;// ---weather07
    private TextView week1_dayTv, week1_temTv, week1_cliTv, week1_windTv,
            week2_dayTv, week2_temTv, week2_cliTv, week2_windTv,
            week3_dayTv, week3_temTv, week3_cliTv, week3_windTv,
            week4_dayTv, week4_temTv, week4_cliTv, week4_windTv,
            week5_dayTv, week5_temTv, week5_cliTv, week5_windTv,
            week6_dayTv, week6_temTv, week6_cliTv, week6_windTv;
    private ImageView weatherImg, pmImg;// ---weather07
    private ImageView week1Img, week2Image,week3Image,week4Image,week5Image,week6Image;
    private String mCurCityCode;//当前选择的城市编码
    private SharedPreferenceUtil mSpUtil;
    private MyApplication mApplication;
    //显示七天天气用到的声明
    private ViewPagerAdapter vpAdapter;//适配器，用于显示ViewPager的内容
    private ViewPager vp;
    private List<View> views;
    private ImageView dots[];
    private int[] ids = {R.id.week_weather_iv1, R.id.week_weather_iv2};//
    private WeekWeather[] weekWeather;//一周天气

    //通过消息机制，将解析的天气对象发给主线程，主线程接收后调用updateTodayWeather来更新UI界面
   private static final int UPDATE_TODAY_WEATHER = 1;// ---weather07
    private Handler mHandler = new Handler() { // ---weather07 //Handler主要有两个用途:首先是可以定时处理或者分发消息，其次是可以添加一个执行的行为在其它线程中执行
        /*消息android.os.Message：
        是定义一个Messge包含必要的描述和属性数据，并且此对象可以被发送给android.os.Handler处理。
        属性字段：arg1、arg2、what、obj、replyTo等；其中arg1和arg2是用来存放整型数据的；what是用来保存消息标示的；obj是Object类型的任意对象；replyTo是消息管理器，
        会关联到一个handler，handler就是处理其中的消息。通常对Message对象不是直接new出来的，只要调用handler中的obtainMessage方法来直接获得Message对象。
        https://www.cnblogs.com/to-creat/p/4964458.html*/
        public void handleMessage(android.os.Message msg){//覆盖handleMessage方法
            switch (msg.what){//根据收到的消息的what类型处理
                //更新今日天气
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    mUpdateBtn.setVisibility(View.VISIBLE);
                    mUpdateProgressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override //onCreate：当活动被创建时执行的方法
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);//在Activity中指定布局文件

        //Buttons
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);//城市管理按钮 ---weather08
        mCitySelect.setOnClickListener(this);// ---weather08
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);//刷新按钮 weather05
        mUpdateBtn.setOnClickListener(this);//weather05
        mUpdateProgressBar = (ProgressBar)findViewById(R.id.title_update_progress);//刷新动画，不需要设置点击事件

        //检查网络状态
        if(NetUtil.getNetworkState((this))!=NetUtil.NETWORN_NONE)
        {
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this,"网络OK!",Toast.LENGTH_LONG).show();//这行是显示在APP里的，toast显示框
        }else
        {
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }
        initViewPager();
        initDots();

        //初始化一些数据
        initData();
        //初始化控件内容
        initView(); //---weather07
        //ViewPager相关的初始化

    }

    //初始化数据
    private void initData(){
        mApplication = MyApplication.getInstance();
        mSpUtil = mApplication.getSharedPreferenceUtil();
        weekWeather = new WeekWeather[6];
        for(int i=0;i<weekWeather.length;i++){
            weekWeather[i] = new WeekWeather();
        }
        //weekWeather[0].setDate("今天");
        //Log.d("myWeather", weekWeather[0].getDate());
    }

    //ViewPager相关的初始化
    private void initViewPager(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(layoutInflater.inflate(R.layout.week_weather_page1,null));
        views.add(layoutInflater.inflate(R.layout.week_weather_page2,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager)findViewById(R.id.week_weather_viewpager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
    }

    private void initDots(){
        dots = new ImageView[views.size()];
        for (int i=0; i<views.size(); i++){
            dots[i]=(ImageView)findViewById(ids[i]);
        }
    }

    //为更新按钮添加单击事件 weather05
    @Override
    public void onClick(View view) {

        //点击城市管理按钮---Weather08-2
        if(view.getId()==R.id.title_city_manager){
            Intent i = new Intent(this,SelectCity.class);//Intent调用另一个Activity
            //startActivity(i);
            startActivityForResult(i,1);
            /*　startActivityForResult(Intent intent, int requestCode);
                第一个参数：一个Intent对象，用于携带将跳转至下一个界面中使用的数据，使用putExtra(A,B)方法，此处存储的数据类型特别多，基本类型全部支持。
                第二个参数：如果>= 0,当Activity结束时requestCode将归还在onActivityResult()中。以便确定返回的数据是从哪个Activity中返回，它用来标识目标activity。
                */
        }

        //点击刷新按钮
        if(view.getId()==R.id.title_update_btn)
        {
            //从SharedPreferences中读取城市的id
            /*SharedPreferences（SP）是一种轻量级的数据存储方式,采用Key/value的方式进行映射，最终会在手机的/data/data/package_name/shared_prefs/目录下以xml的格式存在。
            Sp通常用于记录一些参数配置、行为标记等。
            注意：不要使用Sp去存储量大的数据，否则会大大影响应用性能，甚至出现ANR
            # getSharedPreferences(name, mode)获取一个SharedPreferences
            参数1:name在/data/data/package_name/shared_prefs/目录下生成的文件的名字(如果该文件不存在就会创建，如果存在则更新)
            参数2:mode该文件的访问模式(Context.MODE_PRIVATE:默认的创建模式，只能由创建它的或者UID相同的应用程序访问，其余三种已经废弃)
            */

            //检测是否有网络，如果有就执行“获取网络数据”的函数
            if(NetUtil.getNetworkState((this))!=NetUtil.NETWORN_NONE)
            {
                Log.d("myWeather","网络OK");

//                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
//                String cityCode = sharedPreferences.getString("main_city_code", "101010100");//从SharedPreferences中读取城市的id，如果没有就默认为101010100，，101120510
//                Log.d("myWeather", cityCode + "没有选择默认为北京");

                String cityCode;
                //如果存在SharedPreferences的城市编码为空，就默认设置为北京
                if(TextUtils.isEmpty(mSpUtil.getCurCityCode())){
                    mSpUtil.setCurCityCode("101010100");//把默认城市北京保存到SharedPreferences
                    cityCode = mSpUtil.getCurCityCode();
                    Log.d("SP","获取默认编码为："+ cityCode);
                }else {
                    //mCurCity = mCityDB.getCity(mSpUtil.getCity());
                    cityCode = mSpUtil.getCurCityCode();//获取城市编码
                    Log.d("SP","获取编码为："+ cityCode);
                    queryWeatherCode(cityCode);//获取网络数据
                }
            }else
            {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }

    //使用 获取网络数据 ---weather05
    private void queryWeatherCode(String citycode)
    {
        mUpdateBtn.setVisibility(View.GONE);
        mUpdateProgressBar.setVisibility(View.VISIBLE);

        if(citycode == "-1")//这个没用
            return;
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + citycode;//URL
        Log.d("myWeather",address);

        //子线程：处理除UI之外较费时的操作，如从网上下载数据或者访问数据库
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;//HttpURLConnection是访问HTTP协议的基本功能的类，继承自URLConnection，可用于向指定网站发送GET请求、POST请求。
                TodayWeather todayWeather = null;// ---weather07
                try{
                    URL url = new URL(address);//定义URL
                    con = (HttpURLConnection)url.openConnection();//到URL所引用的远程对象的链接
                    con.setRequestMethod("GET");//GET是从服务器上获取数据，POST是向服务器传送数据
                    con.setConnectTimeout(8000);//设置连接超时：建立连接的时间。如果到了指定的时间，还没建立连接，则报异常
                    con.setReadTimeout(8000);//设置读取超时：已经建立连接，并开始读取服务端资源。如果到了指定的时间，没有可能的数据被客户端读取，则报异常。
                    InputStream in = con.getInputStream();//得到网络返回的输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));//BufferedReader从字符输入流中读取文本，缓冲各个字符，从而实现字符、数组和行的高效读取。
                    StringBuilder response = new StringBuilder();//StringBuilder适用于单线程下在字符缓冲区进行大量操作的情况
                    String str;
                    while((str=reader.readLine())!=null)//读取网络数据并连接成字符串
                    {
                        response.append(str);//字符串连接
                        Log.d("myWeather",str);
                    }
                    String responseStr = response.toString();//返回一个与构建器或缓冲器内容相同的字符串，这个字符串就是读取网络数据得到的信息
                    Log.d("myWeather",responseStr);

                    //parseXML(responseStr);//获取网络数据后，调用解析函数 ---Weather06
                    todayWeather = parseXML(responseStr);//解析网络数据 ---Weather07
                    if(todayWeather != null)//这里更新今日天气信息 ---Weather07
                    {
                        Log.d("myWeather",todayWeather.toString());

                        //使用Message机制主要是为了保证线程之间操作安全，同时不需要关心具体的消息接收者，使消息本身和线程剥离开，这样就可以方便的实现定时、异步等操作。
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;//what是用来保存消息标示的
                        msg.obj = todayWeather;//obj是Object类型的任意对象
                        mHandler.sendMessage(msg);
                    }

                }catch (Exception e)
                {
                    e.printStackTrace();
                }finally {
                    if(con!=null)
                    {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    //编写解析函数，解析出城市名称已经更新的时间信息 ---weather06
//    private void parseXML(String xmldata)
//    {
//        try {
//            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
//            XmlPullParser xmlPullParser = fac.newPullParser();
//            xmlPullParser.setInput(new StringReader(xmldata));
//            int eventType = xmlPullParser.getEventType();
//            Log.d("myWeather", "parseXML");
//            while (eventType != XmlPullParser.END_DOCUMENT) {
//                switch (eventType) {
//                    //判断当前事件是否为文档开始事件
//                    case XmlPullParser.START_DOCUMENT:
//                        break;
//                    //判断当前事件是否为标签元素开始事件
//                    case XmlPullParser.START_TAG:
//                        if (xmlPullParser.getName().equals("city")) {
//                            eventType = xmlPullParser.next();
//                            Log.d("myWeather", "city: " + xmlPullParser.getText());
//                        } else if (xmlPullParser.getName().equals("updatetime")) {
//                            eventType = xmlPullParser.next();
//                            Log.d("myWeather", "updatetime: " + xmlPullParser.getText());
//                        }
//                        break;
//                    //判断当前事件是否为标签元素结束事件
//                    case XmlPullParser.END_TAG:
//                        break;
//                }
//                eventType = xmlPullParser.next();
//            }
//        }catch(XmlPullParserException e) {
//            e.printStackTrace();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//    }

    //将解析的数据存入TodayWeather对象中 ---weather07
    private TodayWeather parseXML(String xmldata)
    {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dataCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

        try {
            //Android中解析XML的方式主要有三种:sax,dom和pull，这里使用pull方法
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();//创建生产XML的pull解析器的工厂
            XmlPullParser xmlPullParser = fac.newPullParser();//使用工厂获取pull解析器
            xmlPullParser.setInput(new StringReader(xmldata));//使用解析器读取当前的xml流，传入InputStream对象 并且设置解码规则需和XML文档中设置的一致
            int eventType = xmlPullParser.getEventType();//获取当前事件的状态
            Log.d("myWeather", "parseXML");
            /* pull解析是以事件为单位解析的，因此要获取一开始的解析标记type，之后通过type判断循环来读取文档
            注意：当解析器开始读取is的时候已经开始了，指针type在xml的第一行开始。
            pull解析是指针从第一行开始读取到最后一行以事件为单位读取的解析方式*/
            while (eventType != XmlPullParser.END_DOCUMENT) {//通过while循环判断是否读取到了文档结束
                switch (eventType) {
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        //判断当前遇到的元素名称是否为resp（这个<resp>在xml文件里起始的地方）
                        if(xmlPullParser.getName().equals("resp"))
                        {
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null)//已有初始化的TodayWeather对象，开始解析下面的数据
                        {
                            //判断当前遇到的元素名称是否为city
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();//获取下一个事件的状态
                                //Log.d("myWeather", "city: " + xmlPullParser.getText());
                                todayWeather.setCity(xmlPullParser.getText());//将数据封装到TodayWeather类中
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                //Log.d("myWeather", "updatetime: " + xmlPullParser.getText());
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("wendu"))
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("shidu"))
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("pm25"))
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("quality"))
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("fengxiang"))
                            {
                                switch (fengxiangCount){
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setFengxiang(xmlPullParser.getText());
                                        weekWeather[1].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                }
                            }else if(xmlPullParser.getName().equals("fengli"))
                            {
                                switch (fengliCount){
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setFengli(xmlPullParser.getText());
                                        weekWeather[1].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                }
                            }else if(xmlPullParser.getName().equals("date"))
                            {
                                switch (dataCount){
                                    case 0://今日日期
                                        eventType = xmlPullParser.next();
                                        todayWeather.setDate(xmlPullParser.getText());
                                        weekWeather[1].setDate(xmlPullParser.getText());
                                        dataCount++;//让dataCount不为零，也就是这些只处理一次
                                        break;
                                    case 1://第2天日期
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setDate(xmlPullParser.getText());
                                        dataCount++;
                                        break;
                                    case 2://第3天日期
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setDate(xmlPullParser.getText());
                                        dataCount++;
                                        break;
                                    case 3://第4天日期
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setDate(xmlPullParser.getText());
                                        dataCount++;
                                        break;
                                    case 4://第5天日期
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setDate(xmlPullParser.getText());
                                        dataCount++;
                                        break;

                                }
                            }else if(xmlPullParser.getName().equals("high"))
                            {
                                switch (highCount){
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());//
                                        weekWeather[1].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                }
                            }else if(xmlPullParser.getName().equals("low"))
                            {
                                switch (lowCount){
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setLow(xmlPullParser.getText().substring(2).trim());//
                                        weekWeather[1].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                }
                            }else if(xmlPullParser.getName().equals("type"))
                            {
                                switch (typeCount){
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setType(xmlPullParser.getText());
                                        weekWeather[1].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                }
                            }//昨天日期
                            else if(xmlPullParser.getName().equals("date_1")){
                                eventType = xmlPullParser.next();
                                Log.d("myWeather",xmlPullParser.getText());//
                                weekWeather[0].setDate(xmlPullParser.getText());
                            }
                            //昨天高温
                            else if(xmlPullParser.getName().equals("high_1"))
                            {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setHigh(xmlPullParser.getText().substring(2).trim());
                            }
                            //昨天低温
                            else if(xmlPullParser.getName().equals("low_1"))
                            {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setLow(xmlPullParser.getText().substring(2).trim());//
                            }
                            //昨天天气状况
                            else if(xmlPullParser.getName().equals("type_1"))
                            {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setType(xmlPullParser.getText());
                            }
                            //昨天风向
                            else if(xmlPullParser.getName().equals("fx_1"))
                            {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setFengxiang(xmlPullParser.getText());
                            }
                            //昨天风力
                            else if(xmlPullParser.getName().equals("fl_1"))
                            {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setFengli(xmlPullParser.getText());
                            }
                        }
                        break;
                    //判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        }catch(XmlPullParserException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        //Log.d("myWeather", weekWeather[0].getDate());
        for (WeekWeather w : weekWeather){
            Log.d("myWeather", w.getDate() + ", " + w.getType() +  ", " +w.getHigh()+ ", "+ w.getLow() + ", " +w.getFengli()+ ", " +w.getFengxiang());
        }
        return todayWeather;//
    }

    //初始化控件内容 ---weather07
    void initView()
    {
        //通过id把定义的控件和UI上的元素关联起来
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        pmImg = (ImageView) findViewById(R.id.pm2_5_image);//pm2.5图片
        weatherImg = (ImageView) findViewById(R.id.weather_img);//天气状况图片

        week1_dayTv = (TextView)views.get(0).findViewById(R.id.week1_day);
        week1_cliTv = (TextView)views.get(0).findViewById(R.id.week1_climate);
        week1_temTv = (TextView)views.get(0).findViewById(R.id.week1_temperature);
        week1_windTv = (TextView)views.get(0).findViewById(R.id.week1_wind);
        week2_dayTv = (TextView)views.get(0).findViewById(R.id.week2_day);
        week2_cliTv = (TextView)views.get(0).findViewById(R.id.week2_climate);
        week2_temTv = (TextView)views.get(0).findViewById(R.id.week2_temperature);
        week2_windTv = (TextView)views.get(0).findViewById(R.id.week2_wind);
        week3_dayTv = (TextView)views.get(0).findViewById(R.id.week3_day);
        week3_cliTv = (TextView)views.get(0).findViewById(R.id.week3_climate);
        week3_temTv = (TextView)views.get(0).findViewById(R.id.week3_temperature);
        week3_windTv = (TextView)views.get(0).findViewById(R.id.week3_wind);
        week4_dayTv = (TextView)views.get(1).findViewById(R.id.week4_day);
        week4_cliTv = (TextView)views.get(1).findViewById(R.id.week4_climate);
        week4_temTv = (TextView)views.get(1).findViewById(R.id.week4_temperature);
        week4_windTv = (TextView)views.get(1).findViewById(R.id.week4_wind);
        week5_dayTv = (TextView)views.get(1).findViewById(R.id.week5_day);
        week5_cliTv = (TextView)views.get(1).findViewById(R.id.week5_climate);
        week5_temTv = (TextView)views.get(1).findViewById(R.id.week5_temperature);
        week5_windTv = (TextView)views.get(1).findViewById(R.id.week5_wind);
        week6_dayTv = (TextView)views.get(1).findViewById(R.id.week6_day);
        week6_cliTv = (TextView)views.get(1).findViewById(R.id.week6_climate);
        week6_temTv = (TextView)views.get(1).findViewById(R.id.week6_temperature);
        week6_windTv = (TextView)views.get(1).findViewById(R.id.week6_wind);

        //把文字控件的值都设为N/A
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        weekTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        week1_dayTv.setText("N/A");
        week1_cliTv.setText("N/A");
        week1_temTv.setText("N/A");
        week1_windTv.setText("N/A");
        week2_dayTv.setText("N/A");
        week2_cliTv.setText("N/A");
        week2_temTv.setText("N/A");
        week2_windTv.setText("N/A");
        week3_dayTv.setText("N/A");
        week3_cliTv.setText("N/A");
        week3_temTv.setText("N/A");
        week3_windTv.setText("N/A");
        week4_dayTv.setText("N/A");
        week4_cliTv.setText("N/A");
        week4_temTv.setText("N/A");
        week4_windTv.setText("N/A");
        week5_dayTv.setText("N/A");
        week5_cliTv.setText("N/A");
        week5_temTv.setText("N/A");
        week5_windTv.setText("N/A");
        week6_dayTv.setText("N/A");
        week6_cliTv.setText("N/A");
        week6_temTv.setText("N/A");
        week6_windTv.setText("N/A");
        //pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
        //weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
    }

    //用TodayWeather对象更新UI控件显示 ---weather07
    void updateTodayWeather(TodayWeather todayWeather)
    {
        //改pm2.5图片
        if(todayWeather.getPm25() != null) {
            int pm25Int = Integer.parseInt(todayWeather.getPm25());
            Log.d("myWeather", String.valueOf(pm25Int));
            pmImg.setVisibility(View.VISIBLE);
            if (pm25Int <= 50) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            } else if (pm25Int > 50 && pm25Int <= 100) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            } else if (pm25Int > 100 && pm25Int <= 150) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            } else if (pm25Int > 150 && pm25Int <= 200) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            } else if (pm25Int > 200 && pm25Int <= 300) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            } else if (pm25Int > 300) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
            }
        }else{
            pmImg.setVisibility(View.INVISIBLE);
        }
        //改天气状况图片
        if(todayWeather.getType() != null) {
            String type = todayWeather.getType();
            weatherImg.setVisibility(View.VISIBLE);
            switch (type) {
                case "暴雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "大雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "大雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "多云":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "雷阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨冰雹":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "晴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "沙尘暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                case "特大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "雾":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "小雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "小雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "阴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雨夹雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "阵雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "中雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "中雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
            }
        }else{
            weatherImg.setVisibility(View.INVISIBLE);
        }
        todayWeather.missData();//将缺失的数据设置为"N/A"

        //文字控件--根据网络数据刷新UI的文字
        city_name_Tv.setText(todayWeather.getCity() + "天气");//红条上的，北京天气
        cityTv.setText(todayWeather.getCity());//布局上左侧的，城市名
        timeTv.setText(todayWeather.getUpdatetime() + "发布");//布局上左侧的，时间
        humidityTv.setText("湿度：" + todayWeather.getShidu());//布局上左侧的，湿度
        pmDataTv.setText(todayWeather.getPm25());//布局上右侧的，pm2.5值
        pmQualityTv.setText(todayWeather.getQuality());//布局上右侧的，空气质量
        weekTv.setText(todayWeather.getDate());//布局中间的，日期
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());//布局中间的，温度
        climateTv.setText(todayWeather.getType());//布局中间的，天气情况
        windTv.setText("风力：" + todayWeather.getFengli());//布局中间的，风力

        week1_dayTv.setText(weekWeather[0].getDate());
        week1_cliTv.setText(weekWeather[0].getType());
        week1_temTv.setText(weekWeather[0].getHigh()+ "~" + weekWeather[0].getLow());
        week1_windTv.setText(weekWeather[0].getFengxiang()+ weekWeather[0].getFengli());
        week2_dayTv.setText(weekWeather[1].getDate());
        week2_cliTv.setText(weekWeather[1].getType());
        week2_temTv.setText(weekWeather[1].getHigh()+ "~" + weekWeather[1].getLow());
        week2_windTv.setText(weekWeather[1].getFengxiang()+ weekWeather[1].getFengli());
        week3_dayTv.setText(weekWeather[2].getDate());
        week3_cliTv.setText(weekWeather[2].getType());
        week3_temTv.setText(weekWeather[2].getHigh()+ "~" + weekWeather[2].getLow());
        week3_windTv.setText(weekWeather[2].getFengxiang()+ weekWeather[2].getFengli());
        week4_dayTv.setText(weekWeather[3].getDate());
        week4_cliTv.setText(weekWeather[3].getType());
        week4_temTv.setText(weekWeather[3].getHigh()+ "~" + weekWeather[3].getLow());
        week4_windTv.setText(weekWeather[3].getFengxiang()+ weekWeather[3].getFengli());
        week5_dayTv.setText(weekWeather[4].getDate());
        week5_cliTv.setText(weekWeather[4].getType());
        week5_temTv.setText(weekWeather[4].getHigh()+ "~" + weekWeather[4].getLow());
        week5_windTv.setText(weekWeather[4].getFengxiang()+ weekWeather[4].getFengli());
        week6_dayTv.setText(weekWeather[5].getDate());
        week6_cliTv.setText(weekWeather[5].getType());
        week6_temTv.setText(weekWeather[5].getHigh()+ "~" + weekWeather[5].getLow());
        week6_windTv.setText(weekWeather[5].getFengxiang()+ weekWeather[5].getFengli());

    }

    //接收城市管理界面返回的数据 ---weather08-2
    /*
    onActivityResult(int requestCode, int resultCode, Intent data)
    第一个参数：这个整数requestCode用于与startActivityForResult中的requestCode中值进行比较判断，是以便确认返回的数据是从哪个Activity返回的。
    第二个参数：这整数resultCode是由子Activity通过其setResult()方法返回。适用于多个activity都返回数据时，来标识到底是哪一个activity返回的值。
    第三个参数：一个Intent对象，带有返回的数据。可以通过data.getXXXExtra( );方法来获取指定数据类型的数据，
    getStringExtra()
    获取Intent对象携带的String类型的数据*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK) {
            if (data.getStringExtra("cityCode") != null) {//如果选择城市不为空
                String newCityCode = data.getStringExtra("cityCode");//cityCode---SelectCity.java里的i.putExtra
                Log.d("myWeather", "选择的城市代码为" + newCityCode);

                if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                    Log.d("myWeather", "网络OK");
                    mSpUtil.setCurCityCode(newCityCode);//选择完城市，设置城市编码
                    queryWeatherCode(newCityCode);
                } else {
                    Log.d("myWeather", "网络挂了");
                    Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        for (int a=0;a<ids.length;a++){
            if(a==i){
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            }else {
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
