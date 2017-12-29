package com.example.administratior.coolweahter.db;

import org.litepal.crud.DataSupport;

/**
 * Created by administratior on 2017/12/29.
 */

//Bean类:记录从服务器获得的城市信息
public class City extends DataSupport{
    private int id;//城市id
    private String cityName;//城市名
    private int cityCode;//城市代号
    private int provinceId;//所在的省的id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
