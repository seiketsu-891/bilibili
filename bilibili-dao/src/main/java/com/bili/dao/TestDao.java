package com.bili.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestDao {
    public String findNameById(Integer id);
}
