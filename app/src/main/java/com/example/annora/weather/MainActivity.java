package com.example.annora.weather;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
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

import cn.pku.edu.wwr.App.MyApplication;
import cn.pku.edu.wwr.bean.City;
import cn.pku.edu.wwr.bean.TodayWeather;
import cn.pku.edu.wwr.db.CityDB;
import cn.pku.edu.wwr.util.NetUtil;
import cn.pku.edu.wwr.util.SharedPreferenceUtil;


/*implements View.OnClickListener添加按钮单击事件
implements是一个类，实现一个接口用的关键字，它是用来实现接口中定义的抽象方法。*/
public class MainActivity extends Activity implements View.OnClickListener{ //项目中所有活动必须继承Activity或它的子类才能拥有活动的特性

    //按钮
    private ImageView mUpdateBtn;//刷新按钮---weather05
    private ImageView mCitySelect;//左上方，选择城市按钮---weather08
    private ProgressBar mUpdateProgressBar;//刷新按钮动画
    //文字控件、图片控件
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv,
            pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;// ---weather07
    private ImageView weatherImg, pmImg;// ---weather07
    private String mCurCityCode;//当前选择的城市编码
    private SharedPreferenceUtil mSpUtil;
    private MyApplication mApplication;

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
        //初始化一些数据
        initData();
        //初始化控件内容
        initView(); //---weather07
    }

    //初始化数据
    private void initData(){
        mApplication = MyApplication.getInstance();
        mSpUtil = mApplication.getSharedPreferenceUtil();
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
                            todayWeather = new TodayWeather();//初始化TodayWeather对象
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
                            }else if(xmlPullParser.getName().equals("pm25"))//这个数据有时候没有！！！
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("quality"))//这个数据有时候没有！！！
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0)
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;//
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 0)
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;//
                            }else if(xmlPullParser.getName().equals("date") && dataCount == 0)
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dataCount++;//
                            }else if(xmlPullParser.getName().equals("high") && highCount == 0)
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());//
                                highCount++;//
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 0)
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());//
                                lowCount++;//
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 0)
                            {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;//
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
}
