package com.example.administratior.coolweahter.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by administratior on 2017/12/29.
 */

//网络访问的工具类
public class HttpUtil{
    //访问服务器返回数据,在CallBack回调接口中处理返回的数据
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        /*
         *访问网络接口的具体逻辑
         */
        OkHttpClient client=new OkHttpClient();//创建Okhttp实例
        //获得request对象
        Request request=new Request.Builder()
                .url(address)
                .build();
        //访问服务器
        client.newCall(request).enqueue(callback);
    }
}
