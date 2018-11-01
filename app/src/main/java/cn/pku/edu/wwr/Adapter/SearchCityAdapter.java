package cn.pku.edu.wwr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.example.annora.weather.R;

import java.util.ArrayList;
import java.util.List;

import cn.pku.edu.wwr.bean.City;

//public int getCount(): 适配器中数据集的数据个数；
//public Object getItem(int position): 获取数据集中与索引对应的数据项；
//public long getItemId(int position): 获取指定行对应的ID；
//public View getView(int position,View convertView,ViewGroup parent): 获取每一行Item的显示内容。
public class SearchCityAdapter extends BaseAdapter{
    private Context mContext;//SelectCity Activity
    private List<City> mCityLists;//所有城市列表
    private List<City> mSearchCityLists;//搜索到的城市列表
    private LayoutInflater mInflater;//布局填充器

    public SearchCityAdapter(Context context, List<City> cityLists){
        mContext = context;
        mCityLists = cityLists;
        mSearchCityLists = new ArrayList<City>();
        mInflater = LayoutInflater.from(mContext);//从一个Context中，获得一个布局填充器，
    }

    @Override
    //返回ListView里面所有的item子项的个数
    public int getCount() {
        return mSearchCityLists.size();
    }

    @Override
    //返回每一个item子项
    public Object getItem(int position) {
        return mSearchCityLists.get(position);
    }

    @Override
    //返回每一个item子项的id
    public long getItemId(int position) { return position; }

    @Override
    //返回每一项的显示内容
    /*第一个参数position----------该视图在适配器数据中的位置
    第二个参数convertView-----旧视图
    第三个参数parent------------此视图最终会被附加到的父级视图*/
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.default_search_city,null);//将XML转化为View ///@@@item
        }
        TextView provinceTv = (TextView)convertView.findViewById(R.id.search_province);//Item布局中对应的控件===省名
        TextView cityTv = (TextView)convertView.findViewById(R.id.search_city);//Item布局中对应的控件===城市名
        provinceTv.setText(mSearchCityLists.get(position).getProvince());//设置控件的属性值-省
        cityTv.setText(mSearchCityLists.get(position).getCity());//设置控件的属性值-城市名
        return convertView;
    }

    public Filter getFilter(){
        Filter filter = new Filter() {
            @Override
            /*根据约束条件(CharSequence)调用一个工作线程过滤数据。Filter的子类必须实现该方法来执行过滤操作。
            过滤结果以Filter.FilterResults的形式返回，然后在UI线程中通过publishResults(CharSequence,android.widget.Filter.FilterResults)方法来发布。
            约定：当约束条件为null时，原始数据必须被恢复。
            这里的约束CharSequence constraint是用户在搜索框输入的字符串*/
            protected FilterResults performFiltering(CharSequence constraint) {
                String str = constraint.toString().toUpperCase();//获得用户输入的字符串---toUpperCase将所有的英文字符转换为大写字母（因为数据库city.db里存的是大写的）
                FilterResults results = new FilterResults();//本函数返回的过滤结果
                ArrayList<City> fliterList = new ArrayList<City>();//存储过滤后留下的数据

                if(mCityLists != null && mCityLists.size()!=0){
                    for(City city : mCityLists){
                        //getAllFirstPY().indexOf(str)：查找指定字符或字符串str在字符串getAllFirstPY()中第一次出现地方的索引，未找到的情况返回 -1.
                        if(city.getAllFirstPY().indexOf(str) > -1 //全拼音（BEIJING）
                                || city.getAllPY().indexOf(str) > -1 //每个字的拼音首字母（BJ）
                                || city.getCity().indexOf(str) > -1){ //城市或区名称（北京）
                            fliterList.add(city);
                        }
                    }
                }
                results.values = fliterList;//过滤操作之后的数据的值
                results.count = fliterList.size();//过滤操作之后的数据的数量

                return results;
            }

            @Override
            //通过调用UI线程在用户界面发布过滤结果。
            // Filter的子类必须实现该方法来显示performFiltering(CharSequence)的过滤结果。
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mSearchCityLists = (ArrayList<City>) results.values;
                if(results.count > 0){
                    notifyDataSetChanged();//如果适配器的内容改变时,强制调用getView来刷新每个Item的内容。
                }else {
                    notifyDataSetInvalidated();//重绘控件（还原到初始状态）
                }
            }
        };
        return filter;
    }
}
