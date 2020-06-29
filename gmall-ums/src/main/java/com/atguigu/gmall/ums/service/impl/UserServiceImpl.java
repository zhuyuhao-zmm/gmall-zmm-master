package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.ums.exception.UserException;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean check(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type){


            //1，用户名；2，手机；3，邮箱
            case 1: wrapper.eq("username", data);break;
            case 2: wrapper.eq("phone", data);break;
            case 3: wrapper.eq("emall", data);break;
        }

        return this.count(wrapper) == 0;
    }

    @Override
    public void register(UserEntity user, String code) {
        //校验验证码 TODO

        //生成盐
        String salt = UUID.randomUUID().toString().substring(0, 6);
        user.setSalt(salt);

        //加盐加密
        //md5 生成64位
        user.setPassword(DigestUtils.md5Hex(user.getPassword() + salt));

        //新增用户
        user.setIntegration(1000);
        user.setStatus(1);
        user.setSourceType(1);
        user.setGrowth(1000);
        user.setLevelId(1L);
        user.setCreateTime(new Date());

        this.save(user);
    }

    @Override
    public UserEntity login(String username, String password) {
        //根据用户名查询，并获取盐
        UserEntity userEntity = getOne(new QueryWrapper<UserEntity>().eq("username", username).or().eq("phone",username).or().eq("email",username));

        //通过组装好盐的加密字符串验证密码
        if (userEntity == null){
           throw  new UserException("用户名错误");
        }
        password = DigestUtils.md5Hex(password + userEntity.getSalt());

        if (!StringUtils.equals(password, userEntity.getPassword())){
            throw  new UserException("密码错误");
        }

        return userEntity;

    }

}