package com.atguigu.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UsermanageService;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UsermanageServiceImpl implements UsermanageService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;


    //查询所有
    public List<UserInfo> getAllUserInfoList() {

        List<UserInfo> userInfos = userInfoMapper.selectAll();

        return userInfos;
    }

    //条件查询
    public List<UserInfo> getUserInfoList(UserInfo userinfoQuery) {

        //List<UserInfo> userInfos1 = userInfoMapper.select(userinfoQuery);

        Example example = new Example(UserInfo.class);

        example.createCriteria().andBetween("id", 3, 6);

        List<UserInfo> userInfos = userInfoMapper.selectByExample(example);

        return userInfos;
    }

    //查询单值
    public UserInfo getUserInfo(UserInfo userInfoQuery) {

        //UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userInfoQuery.getId());

        UserInfo userInfo = userInfoMapper.selectOne(userInfoQuery);

        return userInfo;
    }

    //添加用户
    public void addUserInfo(UserInfo userInfo) {

        //会覆盖数据库默认值
        userInfoMapper.insert(userInfo);

        //不会覆盖数据库默认值
        // userInfoMapper.insertSelective(userInfo);
    }

    //修改用户
    public void updateUserInfo(UserInfo userInfo) {

        //按主键查询，null值会覆盖原值
        // int i = userInfoMapper.updateByPrimaryKey(userInfo);

        //按主键查询，null值不会覆盖原值
        // int i1 = userInfoMapper.updateByPrimaryKeySelective(userInfo);

        Example example = new Example(userInfo.getClass());
        example.createCriteria().andLike("nickName", "%" + userInfo.getNickName() + "%");
        userInfo.setNickName(null);
        //userInfoMapper.updateByExample(userInfo,example);

        //按照example查询，按照userInfo修改，null值不会覆盖数据库
        userInfoMapper.updateByExampleSelective(userInfo, example);
    }


    //删除用户
    public void deleteUserInfo(UserInfo userInfo) {

        userInfoMapper.deleteByPrimaryKey(userInfo);

        //按非空值匹配删除
        // userInfoMapper.delete(userInfo);

    }

    @Override
    public int getCount(UserInfo userInfo) {

        int i = userInfoMapper.selectCount(userInfo);

        return i;
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);

        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);

        return userAddressList;
    }

}
