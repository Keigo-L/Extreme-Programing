package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.ContactPerson;
import com.example.demo.dto.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContactPersonMapper extends BaseMapper<ContactPerson> {
}
