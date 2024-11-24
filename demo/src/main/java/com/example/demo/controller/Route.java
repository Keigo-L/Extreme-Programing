package com.example.demo.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.dto.ContactPerson;
import com.example.demo.dto.User;
import com.example.demo.dto.UserContactPersonCollection;
import com.example.demo.mapper.CollectionMapper;
import com.example.demo.mapper.ContactPersonMapper;
import com.example.demo.mapper.UserMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class Route {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ContactPersonMapper contactPersonMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @GetMapping(value = {"/", "/index"})
    public String index(Model model){
        User user = userMapper.selectById(1);
        model.addAttribute("userId", user.getId());
        model.addAttribute("userName", user.getName());
        List<ContactPerson> contactPeoples = contactPersonMapper.selectList(Wrappers.emptyWrapper());
        List<Map<String, Object>> newContactPeoples = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(contactPeoples)){
            List<Object> contactPersonIds = collectionMapper.selectObjs(Wrappers.lambdaQuery(UserContactPersonCollection.class).eq(UserContactPersonCollection::getUserId, user.getId()).select(UserContactPersonCollection::getContactPersonId));
            contactPeoples.forEach(index -> {
                Map<String, Object> bean =BeanUtil.beanToMap(index);
                if (contactPersonIds.contains(index.getId())){
                    bean.put("isFavorite", true);
                }else{
                    bean.put("isFavorite", false);
                }
                newContactPeoples.add(bean);
            });
        }
        model.addAttribute("contactPeoples", newContactPeoples);
        return "index";
    }

    @GetMapping("/contactPersonPage")
    public String contactPersonPage(Model model){
        return "contact_person";
    }

    @GetMapping("/collection")
    public String collection(String userId, String contactPersonId, Model model){
        UserContactPersonCollection userContactPersonCollection = new UserContactPersonCollection();
        userContactPersonCollection.setUserId(userId);
        userContactPersonCollection.setContactPersonId(contactPersonId);
        Long count = collectionMapper.selectCount(Wrappers.lambdaQuery(UserContactPersonCollection.class).eq(UserContactPersonCollection::getUserId,userId).eq(UserContactPersonCollection::getContactPersonId, contactPersonId));
        if (count > 0){
            collectionMapper.delete(Wrappers.lambdaQuery(UserContactPersonCollection.class).eq(UserContactPersonCollection::getUserId,userId).eq(UserContactPersonCollection::getContactPersonId, contactPersonId));
        }else{
            collectionMapper.insert(userContactPersonCollection);
        }
        return "redirect:" + index(model);
    }

    @GetMapping("/importPage")
    public String importPage(Model model){
        return "import";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        try {
            InputStream inputStream = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(inputStream);
            List<List<Object>> reads = reader.read();
            if (CollectionUtil.isNotEmpty(reads)){
                for (int i = 1; i < reads.size(); i++) {
                    List<Object> objects = reads.get(i);
                    ContactPerson contactPerson = new ContactPerson();
                    contactPerson.setName(objects.get(1).toString());
                    contactPerson.setPhone(objects.get(2).toString());
                    contactPerson.setEmail(objects.get(3).toString());
                    contactPerson.setMateAccount(objects.get(4).toString());
                    contactPerson.setAddress(objects.get(5).toString());
                    contactPersonMapper.insert(contactPerson);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:" + index(model);
    }
}
