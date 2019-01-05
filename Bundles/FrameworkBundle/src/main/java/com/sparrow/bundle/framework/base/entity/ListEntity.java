package com.sparrow.bundle.framework.base.entity;

import java.util.List;

public class ListEntity<T> {
    public PageEntity pageInfo;
    public List<T> items;
    public List<T> mdlist;
    public List<T> data;
    public int count; // 异常总数
}
