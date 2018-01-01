package com.example.administratior.coolweahter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administratior.coolweahter.R;
import com.example.administratior.coolweahter.db.City;
import com.example.administratior.coolweahter.db.County;
import com.example.administratior.coolweahter.db.Province;
import com.example.administratior.coolweahter.util.HttpUtil;
import com.example.administratior.coolweahter.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by administratior on 2017/12/29.
 */

public class ChooseAreaFragment extends Fragment{

    /*
     * 在这个fragment里实现遍历省市县的具体逻辑
     */

    //当前的行政级别
    public static final int LEVEL_PROVINCE=0; //省级
    public static final int LEVEL_CITY=1; //市级
    public static final int LEVEL_COUNTY=2; //县,区,乡级

    //控件
    private static ProgressDialog progressDialog;
    private TextView titleText; //显示当前地名
    private Button backButton; //返回上一层的按钮
    private ListView listView; //显示地名列表

    //数据
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();//Adapter的数据列表
    private List<Province> provinceList; //省列表
    private List<City> cityList; //市列表
    private List<County> countyList; //县,区,乡列表

    private Province selectedProvince; //当前选中的省
    private City selectedCity; //当前选择的市
    private int currentLevel; //当前选择的级别

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        //给listView的子项添加点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //根据当前的行政等级判断转跳的地点
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                   queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                   queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId=countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        //判断当前的activity是否是MainActivity,如果是则转跳到WeatherActivity
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        //不是则代表已在WeatherActivity中,切换城市,去服务获取该城市的天气信息
                        WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();//关闭滑动菜单
                        activity.swipeRefreshLayout.setRefreshing(true);//显示重新刷新
                        activity.requestWeather(weatherId);//切换城市,刷新城市天气信息
                    }
                }
            }
        });
        //给返回按钮添加点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("zzzz","onBackButton");
                //如果当前是县区乡一级则返回市区列表
                if(currentLevel==LEVEL_COUNTY){
                   queryCities();
                }
                //如果是当前是市一级则返回省级列表
                else if(currentLevel==LEVEL_CITY){
                   queryProvinces();
                }
            }
        });
        //第一次进入app则先去查询所有省数据
        queryProvinces();
    }

    //查询全国所有的省,优先从数据库查询,如果没有本地没有数据再去服务器上查询
    private void queryProvinces(){
           //获取所有省的具体逻辑
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);//省级列表页面不能再往回返回,将返回按钮设为不可见
        //先从本地数据库查询数据,如果没有,则去服务器请求数据
        provinceList= DataSupport.findAll(Province.class);
        //判断本地数据库有没有数据
        if(provinceList.size()>0){
            //有数据则添加到Adapter上
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//设置默认选择第一个项目
            currentLevel=LEVEL_PROVINCE;
        }
        //如果本地列表没有数据则发送请求去服务器获取数据
        else{
             String address="http://guolin.tech/api/china";
             queryFromServer(address,"province");
        }
    }

    //查询选中的省份的全部城市,优先从数据库中查询,如果没有则从服务器请求
    private void queryCities(){
        //先从本地数据库中查询
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        //判断本地是否有数据
        if(cityList.size()>0){
            //有数据
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//默认选中第一个
            currentLevel=LEVEL_CITY;//将目前的行政级别设为市级
        }//没有数据,则去服务器获取
        else{
             int provinceCode=selectedProvince.getProvinceCode();

             String address="http://guolin.tech/api/china/"+provinceCode;
             queryFromServer(address,"city");
        }
    }

    //查询选中市内的所有县,优先从数据库中查询,没有则去服务器查询
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        //先从数据库中查询
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        //判断本地数据库是否有数据
        if(countyList.size()>0){
            //如果有
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();//刷新ListView子项
            currentLevel=LEVEL_COUNTY;//将目前的行政等级设置为县,乡,区一级
            listView.setSelection(0);
        }//如果没有,则去服务器获取
        else{
             int provinceCode=selectedProvince.getProvinceCode();//获取当前选中的省份
             int cityCode=selectedCity.getCityCode();//获取当前选中的城市
             String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;//拼接处URL
             queryFromServer(address,"county");
        }
    }

    //从服务器查询数据的具体逻辑,根据传入的地址从服务器上查询省市县数据
    private void queryFromServer(String address,final String type){
          showProgressDialog();//先显示一个对话框,表示正在加载...
          //发送请求
          HttpUtil.sendOkHttpRequest(address, new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                  //请求失败时回调
                  getActivity().runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          closeProgressDialog();
                          Toast.makeText(getContext(),"加载失败..",Toast.LENGTH_SHORT).show();
                      }
                  });
              }
              @Override
              public void onResponse(Call call, Response response) throws IOException {
                  //请求成功时回调
                  String responseText=response.body().string();//json格式
                  boolean result=false;
                  //根据传入的类型调用不同的解析方法
                  if("province".equals(type)){
                      result= Utility.handleProvinceResponse(responseText);
                  }else if("city".equals(type)){
                      result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                  }else if("county".equals(type)){
                      result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                  }
                  if(result){
                      getActivity().runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              closeProgressDialog();
                              if("province".equals(type)){
                                  queryProvinces();
                              }else if("city".equals(type)){
                                  queryCities();
                              }else if("county".equals(type)){
                                  queryCounties();
                              }
                          }
                      });
                  }
              }
          });
    }

    //在请求数据时显示对话框
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    //关闭对话框
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}


