package cn.pku.edu.wwr.App;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zhangqixun.bean.City;
import cn.pku.edu.wwr.db.CityDB;

//---Weather09
public class MyApplication extends Application{
    private static final String TAG="MyAPP";

    private static MyApplication myApplication;
    private CityDB mCityDB;
    private List<City> mCityList;//初始化城市信息列表

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->oncreate");

        myApplication=this;
        mCityDB=openCityDB();//打开数据库
        initCityList();//初始化城市信息列表
    }

    //getInstance()单例模式、不需要实例化
    public static MyApplication getInstance(){
        return myApplication;
    }

    //创建打开数据库的方法
    private CityDB openCityDB(){
        String path="/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db=new File(path);
        Log.d(TAG,path);
        if(!db.exists()){
            String pathfolder="/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder=new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try {
                InputStream is=getAssets().open("city.db");
                FileOutputStream fos=new FileOutputStream(db);
                int len=-1;
                byte[] buffer=new byte[1024];
                while ((len=is.read(buffer))!=-1){
                    fos.write(buffer,0,len);
                    fos.flush();
                }
                fos.close();
                is.close();
            }catch (IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }

    //初始化城市信息列表
    private void initCityList(){
        mCityList=new ArrayList<City>();
        new Thread(new Runnable(){
            @Override
            public void run(){
                prepareCityList();
            }
        }).start();
    }
    private boolean prepareCityList(){
        mCityList=mCityDB.getAllCity();//调用CityDB.java的方法
        int i=0;
        for(City city: mCityList){
            i++;
            String cityName=city.getCity();
            String cityCode=city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }
    public List<City> getCityList(){
        return mCityList;
    }
}
