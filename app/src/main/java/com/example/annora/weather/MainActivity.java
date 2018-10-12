package com.example.annora.weather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import cn.edu.pku.zhangqixun.bean.TodayWeather;
import cn.pku.edu.wwr.util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener{ //项目中所有活动必须继承Activity或它的子类才能拥有活动的特性

    private ImageView mUpdateBtn;//weather05

    //文字控件、图片控件
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv,
            pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;// ---weather07
    private ImageView weatherImg, pmImg;// ---weather07

    //通过消息机制，将解析的天气对象发给主线程，主线程接收后调用updateTodayWeather来更新UI界面
   private static final int UPDATE_TODAY_WEATHER = 1;// ---weather07
    private Handler mHandler = new Handler() { // ---weather07
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
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

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);//weather05
        mUpdateBtn.setOnClickListener(this);//weather05

        if(NetUtil.getNetworkState((this))!=NetUtil.NETWORN_NONE)
        {
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this,"网络OK!",Toast.LENGTH_LONG).show();//这行是显示在APP里的
        }else
        {
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }
        initView();//初始化控件内容 ---weather07
    }

    //为更新按钮添加单击事件 weather05
    @Override
    public void onClick(View view) {
        //如果点击的按钮id是刷新按钮的id
        if(view.getId()==R.id.title_update_btn)
        {
            //从SharedPreferences中读取城市的id
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101160101");//从SharedPreferences中读取城市的id，如果没有就默认为101010100
            Log.d("myWeather",cityCode);

            //检测是否有网络，如果有就执行“获取网络数据”的函数
            if(NetUtil.getNetworkState((this))!=NetUtil.NETWORN_NONE)
            {
                Log.d("myWeather","网络OK");
                queryWeatherCode(cityCode);//获取网络数据
            }else
            {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }

    //使用 获取网络数据 weather05
    private void queryWeatherCode(String citycode)
    {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + citycode;//URL
        Log.d("myWeather",address);

        //子线程：处理除UI之外较费时的操作，如从网上下载数据或者访问数据库
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;// ---weather07
                try{
                    URL url = new URL(address);//定义URL
                    con = (HttpURLConnection)url.openConnection();//到URL所引用的远程对象的链接
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);//设置连接超时
                    con.setReadTimeout(8000);//设置读取超时
                    InputStream in = con.getInputStream();//得到网络返回的输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine())!=null)
                    {
                        response.append(str);
                        Log.d("myWeather",str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather",responseStr);

                    //parseXML(responseStr);//获取网络数据后，调用解析函数 ---Weather06
                    todayWeather = parseXML(responseStr);//解析网络数据 ---Weather07
                    if(todayWeather != null)// ---Weather07
                    {
                        Log.d("myWeather",todayWeather.toString());

                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
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
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp"))
                        {
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null)
                        {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                //Log.d("myWeather", "city: " + xmlPullParser.getText());
                                todayWeather.setCity(xmlPullParser.getText());
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

        //改pm2.5图片
        int pm25Int = Integer.parseInt(todayWeather.getPm25());
        Log.d("myWeather", String.valueOf(pm25Int));
        if(pm25Int<=50) {
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        }else if(pm25Int>50 && pm25Int<=100) {
            pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
        }else if(pm25Int>100 && pm25Int<=150){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
        }else if(pm25Int>150 && pm25Int<=200){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
        }else if(pm25Int>200 && pm25Int<=300){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
        }else if(pm25Int>300) {
            pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }

        //改天气状况图片
        String type = todayWeather.getType();
        switch (type)
        {
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
    }
}
