package cn.pku.edu.wwr.bean;

//---Weather09
//读取的数据库数据存成City对象
public class City {
    //数据库中每条城市数据包含下面内容
    private String province;//省名称（北京）
    private String city;//城市或区名称（北京）
    private String number;//城市编号
    private String firstPY;//拼音首字母（B）
    private String allPY;//每个字的拼音首字母（BJ）
    private String allFirstPY;//全拼音（BEIJING）

    //构造函数
    public City(String province,String city,String number,String firstPY,String allPY,String allFirstPY){
        this.province = province;
        this.city = city;
        this.number = number;
        this.firstPY = firstPY;
        this.allPY = allPY;
        this.allFirstPY = allFirstPY;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getNumber() {
        return number;
    }

    public String getFirstPY() {
        return firstPY;
    }

    public String getAllPY() {
        return allPY;
    }

    public String getAllFirstPY() {
        return allFirstPY;
    }
}
