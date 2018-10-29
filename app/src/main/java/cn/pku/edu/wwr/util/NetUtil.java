package cn.pku.edu.wwr.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtil {
    public static final int NETWORN_NONE = 0;//final表示这个句柄是不可改变的
    public static final int NETWORN_WIFI = 1;
    public static final int NETWORN_MOBILE=2;

    //获取当前网络状态
    /*
    #  ConnectivityManager：
    主要管理和网络连接相关的操作
    想访问网络状态，首先得添加权限<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    https://blog.csdn.net/jason_wzn/article/details/71131544
    其接口：getActiveNetworkInfo():获取当前激活的网络连接信息
    #  getSystemService：
    获取系统服务；是Activity的一个方法，根据传入的NAME来取得对应的Object，然后转换成相应的服务对象。
    #  CONNECTIVITY_SERVICE：
    网络连接的服务；返回的对象：Connectivity
    */
    public static int getNetworkState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();//需要在AndroidManifest.xml里添加访问网络状态的权限
        if (networkInfo == null) {//无网络
            return NETWORN_NONE;
        }

        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            return NETWORN_MOBILE;
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            return NETWORN_WIFI;
        }
        return NETWORN_NONE;
    }
}

