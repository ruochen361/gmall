package com.atguigu.gmall.usermanage.controller;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UsermanageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class UsermanageController {

    @Autowired
    UsermanageService usermanageService;


    /**
     * @return
     */
    @RequestMapping("/alluserlist")
    public ResponseEntity<List<UserInfo>>  getUserList(){
        List<UserInfo> allUserInfoList = usermanageService.getAllUserInfoList();

        return ResponseEntity.ok().body(allUserInfoList);
    }

    /**
     * @param userInfo
     * @return
     */
    @RequestMapping(value ="/user",method=RequestMethod.GET)
    public  ResponseEntity<UserInfo> getUserInfo(UserInfo userInfo){

        UserInfo userInfo1 = usermanageService.getUserInfo(userInfo);

        return ResponseEntity.ok().body(userInfo1);
    }

    /**
     * @param userInfo
     * @return
     */
    @RequestMapping(value = "/user",method = RequestMethod.POST)
    public ResponseEntity<Void>  addUserInfo(UserInfo userInfo){

        usermanageService.addUserInfo(userInfo);

        return ResponseEntity.ok().build();
    }


    @RequestMapping(value = "/user", method = RequestMethod.PUT)
    public void  updateUserInfo(UserInfo userInfo){
        usermanageService.updateUserInfo(userInfo);
    }


    @RequestMapping(value = "/user",method = RequestMethod.DELETE)
    public  void deleteUserInfo(UserInfo userInfo){

        usermanageService.deleteUserInfo(userInfo);

    }


}
