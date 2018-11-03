package cn.pku.edu.wwr.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.pku.edu.wwr.bean.City;

//CityDB操作类---Weather09
public class CityDB {
    public static final String CITY_DB_NAME = "city.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;
    /*Android提供了一个名为SQLiteDatabase的类，该类封装了一些操作数据库的API，
    使用该类可以完成对数据进行添加(Create)、查询(Retrieve)、更新(Update)和删除(Delete)操作*/

    //打开或创建SQLite数据库
    public CityDB(Context context, String path){
        db=context.openOrCreateDatabase(path,Context.MODE_PRIVATE,null);//打开或创建一个数据库
    }

   /* rawQuery()方法用于执行select语句
    rawQuery()方法的第一个参数为select语句；
    第二个参数为select语句中占位符参数的值，如果select语句没有使用占位符，该参数可以设置为null。
    # execSQL()方法可以执行insert、delete、update和CREATE TABLE之类有更改行为的SQL语句
    # 关于Cursor： 是每行的集合，是一个随机的数据源。所有的数据都是通过下标取得。https://www.cnblogs.com/to-creat/p/5189634.html*/
    public List<City> getAllCity(){
        List<City> list = new ArrayList<City>();
        Cursor c=db.rawQuery("SELECT * from "+ CITY_TABLE_NAME,null);//from后面必须有个空格
        while (c.moveToNext()){//移动光标到下一行
            //读数据库中的一行数据
            String province=c.getString(c.getColumnIndex("province"));//getColumnIndex返回指定列的名称，如果不存在返回-1
            String city=c.getString(c.getColumnIndex("city"));
            String number=c.getString(c.getColumnIndex("number"));
            String allPY=c.getString(c.getColumnIndex("allpy"));
            String allFirstPY=c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY=c.getString(c.getColumnIndex("firstpy"));
            City item=new City(province,city,number,firstPY,allPY,allFirstPY);//调用City的构造函数创建一个City对象
            list.add(item);
        }
        return list;
    }
}
