package com.huangjun.common.service;

import java.util.List;

public interface BaseService<T> {
    int insert(T t);
    int updateOne(T t);
    int deleteOne(T t);
    int updateBatch(List<T> list);
    int deleteBatch(List<T> list);
    List<T> selectById(int id);
    List<T> selectAll();
}
