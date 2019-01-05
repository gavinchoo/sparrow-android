package com.sparrow.bundle.framework.base.entity;

import android.databinding.BaseObservable;

import java.util.List;

//省市区
public class ProvinceEntity extends BaseObservable {

    public String code;      //	省值
    public String name;    //省名
    public String type;  //
    public String parentCode; //
    public List<city> childs;

    public class city
    {
        public String code;      //
        public String name;    //省名
        public String type;  //
        public String parentCode; //
        public List<child> childs;
    }

    public class child
    {
        public String code;
        public String name;
        public String type;
        public String parentCode;
    }
}
