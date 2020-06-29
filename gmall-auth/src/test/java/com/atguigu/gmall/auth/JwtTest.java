package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "D:\\project\\6.9\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\project\\6.9\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "adsfasdf@{+}adsfjASKLDJF))(ASDIF&@#234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1OTE5MTI4MTd9.DeHJpAR3mFhKASz-yYiY_v2OFNbZqOQSdKwxiMMp_OfK0YuGEGU67l1jMwZd3THJ87mCpTSbvhCTM_7LRkILXC7qLgfZKfT9gRWRQqZEo2UeATXol6-pMrypsGgzABVXZNmOI6YWPVNXi3ucj2-C6HHZo0XeKCsXdj_qpvxFRbk21CkiOgT7MwrW0STioSDIyowJkP3OGiD413tfXGwO5xLkfm_VSLXEQEQM8Q8fqMriSQyW8EO4jx8xkiFtX61BdCBAKGyObzndnu7qH9s_nbVhP0NgqfLF7GbC8MoWJ5_3j9nkJNK-k2HryLzZEdbpUXFIx6o5McBcSLrHKnuLkg";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}