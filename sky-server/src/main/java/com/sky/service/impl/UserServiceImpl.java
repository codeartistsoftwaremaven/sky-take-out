package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    public UserServiceImpl(WeChatProperties weChatProperties) {
        this.weChatProperties = weChatProperties;
    }

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid=getOpenid(userLoginDTO.getCode());
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        User user=userMapper.getByOpenid(openid);

        if(user==null){
            user=User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /*private String getOpenid(String code){
        Map<String,String> map=new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json=HttpClientUtil.doGet(WX_LOGIN_URL,map);

        JSONObject jsonObject=JSON.parseObject(json);
        String openid=jsonObject.getString("openid");
        return openid;
    }*/

    private String getOpenid(String code) {
        // 1. 打印传入的code和配置的appid/secret（确认参数是否正确注入）
        String appid = weChatProperties.getAppid();
        String secret = weChatProperties.getSecret();
        log.info("【微信登录】开始调用jscode2session接口，传入code：{}", code);
        log.info("【微信登录】当前配置的appid：{}", appid); // 重点：检查是否为小程序真实ID
        log.info("【微信登录】当前配置的secret：{}", secret); // 重点：检查是否对应appid

        // 2. 组装请求参数（原有逻辑保留）
        Map<String, String> map = new HashMap<>();
        map.put("appid", appid);
        map.put("secret", secret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

        String json = null;
        try {
            // 3. 打印完整请求URL（可直接复制到浏览器测试，快速验证接口是否正常）
            String requestUrl = WX_LOGIN_URL + "?appid=" + appid + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
            log.info("【微信登录】调用微信接口的完整URL：{}", requestUrl);

            // 4. 调用微信接口（原有逻辑保留，增加异常捕获）
            log.info("【微信登录】开始发送HTTP GET请求到微信接口");
            json = HttpClientUtil.doGet(WX_LOGIN_URL, map);
            log.info("【微信登录】微信接口返回原始结果：{}", json); // 关键：查看是否有errcode错误码

            // 5. 解析返回结果（原有逻辑保留，增加错误判断）
            if (json == null || json.isEmpty()) {
                log.error("【微信登录】微信接口返回空结果，可能是网络超时或接口不可达");
                return null;
            }

            JSONObject jsonObject = JSON.parseObject(json);
            // 微信接口返回错误时，会包含"errcode"字段（如40029=code无效、40013=appid错误）
            if (jsonObject.containsKey("errcode")) {
                String errCode = jsonObject.getString("errcode");
                String errMsg = jsonObject.getString("errmsg");
                log.error("【微信登录】微信接口调用失败，errcode：{}，errmsg：{}", errCode, errMsg);
                return null;
            }

            // 6. 解析openid并返回（原有逻辑保留）
            String openid = jsonObject.getString("openid");
            log.info("【微信登录】成功获取openid：{}", openid);
            return openid;

        } catch (Exception e) {
            // 7. 捕获HTTP调用异常（如网络问题、工具类错误）
            log.error("【微信登录】调用微信jscode2session接口时发生异常", e);
            return null;
        }
    }
}
