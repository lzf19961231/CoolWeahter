package com.example.administratior.coolweahter.util;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administratior.coolweahter.R;
import com.example.administratior.coolweahter.db.City;
import com.example.administratior.coolweahter.db.County;
import com.example.administratior.coolweahter.db.Province;

import java.util.ArrayList;
import java.util.List;

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
    private List<String> dataList=new ArrayList<>();
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
                if(currentLevel==LEVEL_PROVINCE){
                   selectedProvince=provinceList.get(position);

                }else if(currentLevel==LEVEL_CITY){

                }
            }
        });
        //给返回按钮添加点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果当前是县区乡一级则返回市区列表
                if(currentLevel==LEVEL_COUNTY){

                }
                //如果是当前是市一级则返回省级列表
                else if(currentLevel==LEVEL_CITY){

                }
            }
        });
        //第一次进入app则显示省列表

    }

    //查询全国所有的省,优先从数据库查询,如果没有本地没有数据再去服务器上查询
    private void queryCities(){

    }
}


