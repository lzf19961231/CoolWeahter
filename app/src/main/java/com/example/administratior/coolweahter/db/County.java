package com.example.administratior.coolweahter.db;

import org.litepal.crud.DataSupport;

/**
 * Created by administratior on 2017/12/29.
 */

//Bean类:用于记录从服务器获得县,区,乡的信息
public class County extends DataSupport{
    private int id;//县,区,乡的id
    private String countyName;//县,区,乡的名字
    private String weatherId;//对应天气的id
    private int cityId;//县,区,乡所属市的id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
