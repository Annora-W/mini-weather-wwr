package cn.pku.edu.wwr.App;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.pku.edu.wwr.bean.City;
import cn.pku.edu.wwr.db.CityDB;
import cn.pku.edu.wwr.util.SharedPreferenceUtil;

//---Weather09
public class MyApplication extends Application{
    private static final String TAG="MyAPP";
    private static MyApplication myApplication;
    private CityDB mCityDB;
    private List<City> mCityList;//初始化城市信息列表//读取的每条数据库信息存在这里
    private SharedPreferenceUtil mSpUtil;
    // 首字母对应的位置
    //private Map<String, Integer> mIndexer;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->oncreate");

        myApplication=this;
        mCityDB=openCityDB();//打开数据库
        initCityList();//初始化城市信息列表
        mSpUtil = new SharedPreferenceUtil(this, SharedPreferenceUtil.CITY_SHAREPRE_FILE);
    }

    //getInstance()单例模式、不需要实例化
    public static MyApplication getInstance(){
        return myApplication;
    }

    //创建打开数据库的方法
    private CityDB openCityDB(){
        String path="/data" //数据库city.db的路径 /data/data/com.example.annora.weather/databases1/city.db
                + Environment.getDataDirectory().getAbsolutePath()//Environment.getDataDirectory()手机内部存储，getAbsolutePath绝对路径
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db=new File(path);
        Log.d(TAG,path);///data/data/com.example.annora.weather/databases1/city.db
        if(!db.exists()){//如果数据库不存在
            String pathfolder="/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder=new File(pathfolder);
            if(!dirFirstFolder.exists()){//如果数据库文件不存在
                dirFirstFolder.mkdirs();//创建这个文件目录
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try {
                InputStream is=getAssets().open("city.db");//读assets文件夹里面名为"city.db"的文件
                FileOutputStream fos=new FileOutputStream(db);//使用File对象打开本地文件，从文件读取数据
                int len=-1;
                byte[] buffer=new byte[1024];
                while ((len=is.read(buffer))!=-1){
                    /*使用write(byte[] b,int off,int len)方法写入文件。
                    该方法将len个字节的数据写入数据库，并从数组b的off位置开始写入到输出流。*/
                    fos.write(buffer,0,len);//写入数据库
                    fos.flush();//清空缓冲区数据
                    /*flush() 是清空的意思。 一般主要用在IO中，即清空缓冲区数据。使用读写流的时候，数据是先被读到了内存中，然后用数据写到文件中。
                    当数据读完的时候不代表你的数据已经写完了，因为还有一部分有可能会留在内存这个缓冲区中。
                    这时候如果调用了close()方法关闭了读写流，那么这部分数据就会丢失，所以应该在关闭读写流之前先flush()，先清空数据。*/
                }
                fos.close();//关闭输出流文件
                is.close();//关闭输入流文件
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
        mCityList = mCityDB.getAllCity();//调用CityDB.java的方法
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
    //返回城市列表
    public List<City> getCityList(){
        return mCityList;
    }

    public synchronized SharedPreferenceUtil getSharedPreferenceUtil(){
        if(mSpUtil == null){
            mSpUtil = new SharedPreferenceUtil(this, SharedPreferenceUtil.CITY_SHAREPRE_FILE);
        }
        return mSpUtil;
    }

    //
//    public Map<String, Integer> getIndexer() {
//        return mIndexer;
//    }
}
