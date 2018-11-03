package cn.pku.edu.wwr.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {
    public SharedPreferences sp;
    public SharedPreferences.Editor spEditor;
    public static final String CITY_SHAREPRE_FILE = "city";
    public static final String CURR_CITY_CODE = "curCityCode";//当前主界面显示城市的编码

    public SharedPreferenceUtil(Context context, String file){
        sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    //设置当前城市的编码
    public void setCurCityCode(String curCityCode){
        spEditor.putString(CURR_CITY_CODE, curCityCode);//以键值对形式写入Editor
            spEditor.commit();//提交数据
    }
    //获取当前城市的编码
    public String getCurCityCode(){
        return sp.getString(CURR_CITY_CODE, "");
    }
}
