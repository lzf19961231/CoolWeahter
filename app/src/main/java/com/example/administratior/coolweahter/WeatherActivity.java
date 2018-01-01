package com.example.administratior.coolweahter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administratior.coolweahter.gson.Forecast;
import com.example.administratior.coolweahter.gson.Weather;
import com.example.administratior.coolweahter.util.HttpUtil;
import com.example.administratior.coolweahter.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;//标题文本框,显示城市

    private TextView titleUpdateTime;//显示目前日期

    private TextView degreeText;//显示温度

    private TextView weatherInfoText;//显示天气信息

    private LinearLayout forecastLayout;//未来几天的天气预报

    private TextView aqiText;//空气质量

    private TextView pm25Text;//PM2.5信息

    private TextView comfortText;//舒适度

    private TextView carWashText;//洗车建议

    private TextView sportText;//运动建议

    private ImageView bingPicImg;//背景图片

    public DrawerLayout drawerLayout;//滑动菜单

    private Button nav_button;//显示滑动菜单按钮

    public SwipeRefreshLayout swipeRefreshLayout;//下拉刷新控件

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将背景状态栏融合
        //判断系统的版本号,此功能只有android 5.0以上才支持
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        //初始化控件
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);//设置下拉刷新的进度条颜色
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        nav_button=(Button)findViewById(R.id.nav_button);

        //显示滑动菜单
        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //获得SharedPreferences,存储天气信息数据
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        final String weatherId;//记录当前城市的weatherId,用于拉下时刷新用
        //判断本地有没天气数据
        if(weatherString!=null){
            //如果有则直接显示到界面
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //没有则去服务器查询
             weatherId=getIntent().getStringExtra("weather_id");
             weatherLayout.setVisibility(View.INVISIBLE);
             requestWeather(weatherId);
        }
        //加载背景图片,如果本地有则直接加载本地,没有则去服务器请求
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            //有则直接加载本地的背景图片
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            //请求服务器数据
            loadBingPic();
        }
        //为下拉刷新控件添加下拉刷新功能,下拉时根据weatherId重新去服务器请求天气数据
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){
                requestWeather(weatherId);
            }
        });
    }

    //根据天气的id请求查询天气信息
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=3dd3ad8e87334064a3517018c0aa6d59";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败时回调
                e.printStackTrace();
                //在主线程中进行UI操作
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);//请求失败结束下拉刷新
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //请求成功时回调
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);//处理返回的天气数据
                runOnUiThread(new Runnable() {
                    //修改UI状态需要在主线程中进行
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            //将其存入到本地
                           SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                           editor.putString("weather",responseText);
                           editor.apply();
                           //展示数据到控件上
                           showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);//请求成功结束下拉刷新
                    }
                });
            }
        });
        loadBingPic();//加载背景图片
    }

    //处理并展示Weather实体类中的数据
    public void showWeatherInfo(Weather weather){
        //从进过解析的数据取出数据,并将其显示在控件上
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"°C";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度:"+weather.suggestion.comfort.info;
        String carWash="洗车指数"+weather.suggestion.carWash.info;
        String sport="运动建议"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    //请求背景图片
    public void loadBingPic(){
        //请求具体逻辑
        String address="http://guolin.tech/api/bing_pic";//请求地址
        //发送请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败时候回调,弹出失败提示
                Toast.makeText(WeatherActivity.this,"加载失败...",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                //请求成功时回调,将请求到的图片保存的本地,并且将其展示在ImageView上

                final String bingPic=response.body().string();//请求成功返回图片的URL
                //获得SharedPreference将其保存到本地
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();//提交保存到本地
                //切换到本地线程,将其显示到控件
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}

