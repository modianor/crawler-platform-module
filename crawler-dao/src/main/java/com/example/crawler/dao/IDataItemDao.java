package com.example.crawler.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface IDataItemDao {
    int insertTableData(@Param("maps") Map<String, Object> maps, @Param("pkName") String pkName, @Param("tableName") String tableName);

    int getCount(@Param("maps") Map<String, Object> maps, @Param("pkName") String pkName, @Param("tableName") String tableName);

    int updateTableData(@Param("maps") Map<String, Object> maps, @Param("pkName") String pkName, @Param("tableName") String tableName);
}
