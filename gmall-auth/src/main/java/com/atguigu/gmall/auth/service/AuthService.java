package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmalUmsFeign;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.exception.UserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private GmalUmsFeign umsFeign;
    public void accredit(String loginName, String password, HttpServletRequest request, HttpServletResponse response){
        try {
            ResponseVo<UserEntity> userEntityResponseVo = umsFeign.login(loginName, password);
            UserEntity userEntity = userEntityResponseVo.getData();

            if (userEntity == null){
                throw new UserException("用户名或者密码有误");
            }

            Map<String, Object> map = new HashMap<>();
            map.put("userId", userEntity.getId());
            map.put("username", userEntity.getUsername());
            map.put("ip", IpUtil.getIpAddressAtService(request));
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire() * 60);
            CookieUtils.setCookie(request, response, jwtProperties.getUnick(), userEntity.getNickname(), jwtProperties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserException("服务器错误");
        }
    }
}
