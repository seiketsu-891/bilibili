package com.bili.service;

import com.bili.dao.TestDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TestService {
   @Resource
    private TestDao testDao;

   public  String getName(Integer id){
       return testDao.findNameById(id);
   }
}
