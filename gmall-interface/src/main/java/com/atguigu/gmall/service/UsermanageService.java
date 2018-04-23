package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date:2018/4/8 0008
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public interface UsermanageService {


    public List<UserInfo> getAllUserInfoList();

    public List<UserInfo> getUserInfoList(UserInfo userinfoQuery);

    public UserInfo getUserInfo(UserInfo userInfoQuery);

    public void addUserInfo(UserInfo userInfo);

    public void updateUserInfo(UserInfo userInfo);

    public void deleteUserInfo(UserInfo userInfo);

    public int getCount(UserInfo userInfo);

    public List<UserAddress> getUserAddressList(String userId);

    public UserInfo login(UserInfo userInfo);

    public UserInfo verify(String userId);
}
