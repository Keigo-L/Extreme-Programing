package com.example.demo.controller;


import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.dto.ContactPerson;
import com.example.demo.mapper.CollectionMapper;
import com.example.demo.mapper.ContactPersonMapper;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class Common {

    @Resource
    private ContactPersonMapper contactPersonMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @GetMapping("/save")
    @ResponseBody
    public Map<String, Object> save(String name, String phone, String email, String mateAccount, String address){
        ContactPerson contactPerson = new ContactPerson();
        contactPerson.setName(name);
        contactPerson.setPhone(phone);
        contactPerson.setEmail(email);
        contactPerson.setMateAccount(mateAccount);
        contactPerson.setAddress(address);
        contactPersonMapper.insert(contactPerson);
        return packages(1, "成功", null);
    }

    @GetMapping("/export")
    public void exportContactPerson(HttpServletResponse response) throws IOException {
        List<ContactPerson> contactPeople = contactPersonMapper.selectList(Wrappers.emptyWrapper());
        ExcelWriter writer = ExcelUtil.getWriter();
        Map<String, String> heads = new LinkedHashMap<>();
        heads.put("id","序号");
        heads.put("name","姓名");
        heads.put("phone","电话号码");
        heads.put("email","电子邮件地址");
        heads.put("mateAccount","社交媒体账户");
        heads.put("address","地址");
        writer.setHeaderAlias(heads);
        Font headFont = writer.getWorkbook().createFont();
        headFont.setFontHeightInPoints((short) 16);
        headFont.setBold(true);
        writer.getStyleSet().getHeadCellStyle().setFont(headFont);
        Font contentFont = writer.getWorkbook().createFont();
        contentFont.setFontHeightInPoints((short) 12);
        writer.getStyleSet().getCellStyle().setFont(contentFont);
        for (int i = 0; i < 6; i++) {
            writer.setColumnWidth(i, 25);
            writer.setRowHeight(i, 30);
        }
        writer.merge(5, "联系人表");
        writer.write(contactPeople, true);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename=contact_person.xls");
        ServletOutputStream out=response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }

    private Map<String, Object> packages(int status, String msg,Object data){
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("msg", msg);
        map.put("data", data);
        return map;
    }
}
