package com.example.administratior.coolweahter.util;

import android.text.TextUtils;

import com.example.administratior.coolweahter.db.City;
import com.example.administratior.coolweahter.db.County;
import com.example.administratior.coolweahter.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by administratior on 2017/12/29.
 */

public class Utility{
    //解析处理服务器返回的省的数据
    public static boolean handleProvinceResponse(String response){
        //Params:String response:服务器返回的数据
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject jsonObject=allProvinces.getJSONObject(i);
                    //使用litepal将服务器返回的数据存储到数据库中
                    Province province=new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.save();
                }
                return true;//成功则返回true
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;//失败则返回false
    }
    //解析处理服务器返回市级的数据
    public static boolean handleCityResponse(String response,int provinceId){
        //Params:String response:服务器返回的数据 int provinceId:所在省的id
        if(!TextUtils.isEmpty(response)){
           try{
               JSONArray array=new JSONArray(response);
               for(int i=0;i<array.length();i++){
                   JSONObject object=array.getJSONObject(i);
                   //使用LitePal将数据保存到本地数据库
                   City city=new City();
                   city.setCityName(object.getString("name"));
                   city.setCityCode(object.getInt("id"));
                   city.setProvinceId(provinceId);
                   city.save();
               }
               return true;//成功则返回true
           }catch (Exception e){
               e.printStackTrace();//失败则返回false
           }
        }
        return false;
    }
    //解析处理服务器返回的县,区,乡数据
    public static boolean handleCountyResponse(String response,int cityId){
        //Params:String response:服务器放的数据 int cityId:所在城市的id
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();i++){
                    JSONObject object=allCounties.getJSONObject(i);
                    //使用Litepal将数据保存的本地数据库
                    County county=new County();
                    county.setCountyName(object.getString("name"));
                    county.setWeatherId(object.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;//成功则返回true
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;//失败则返回false
    }
}
