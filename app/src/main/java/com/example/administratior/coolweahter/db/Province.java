package com.example.administratior.coolweahter.db;

import org.litepal.crud.DataSupport;

/**
 * Created by administratior on 2017/12/29.
 */

//Bean类:用于存放从服务器获得的province信息
public class Province extends DataSupport{
    private int id;//记录省的id
    private int provinceName;//记录省名
    private int provinceCode;//记录省的代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(int provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
