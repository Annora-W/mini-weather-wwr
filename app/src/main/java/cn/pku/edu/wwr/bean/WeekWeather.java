package cn.pku.edu.wwr.bean;

public class WeekWeather {
    private String date;//日期
    private String type;//天气状况
    private String high;//高温
    private String low;//低温
    private String fengli;//风力
    private String fengxiang;//风向

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getHigh() {
        return high;
    }

    public String getLow() {
        return low;
    }

    public String getFengli() {
        return fengli;
    }

    public String getFengxiang() {
        return fengxiang;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public void setFengli(String fengli) {
        this.fengli = fengli;
    }

    public void setFengxiang(String fengxiang) {
        this.fengxiang = fengxiang;
    }

    @Override
    public String toString() {
        return "WeekWeather{" +
                "date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", high='" + high + '\'' +
                ", low='" + low + '\'' +
                ", fengli='" + fengli + '\'' +
                ", fengxiang='" + fengxiang + '\'' +
                '}';
    }
}
