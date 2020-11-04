package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.utils.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class Auth_interceptor extends HandlerInterceptorAdapter {

    // 多个拦截器执行的顺序？
    // 跟配置文件中，配置拦截器的顺序有关系 1 ，2
    // 用户进入控制器之前！
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如何获取到token ？
        // 用户在登录完成之后 会返回一个 url
        // https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
        String token = request.getParameter("newToken");
        // 将token 放入cookie 中！
//        Cookie cookie = new Cookie("token",token);
//        response.addCookie(cookie);
        // 当token不为空的时候，放cookie
        if (token!=null){
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        // 当登录之后，用户访问其他模块，url并没有newToken，但是后台可能将token 放入了cookie 中！
        if (token==null){
            token = CookieUtil.getCookieValue(request,"token",false);
        }
        // 从cookie 中获取token，解密token！
        if (token!=null){
            // 开始解密token 获取nickName
            Map map = getUserMapByToken(token);
            // 取出用户昵称
            String nickName = (String) map.get("nickName");
            // 保存到作用域
            System.out.println("nickName============"+nickName);
            request.setAttribute("nickName",nickName);
        }

        // 在拦截器中获取方法上的注解！
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法上的注解LoginRequire
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation!=null){
            // 此时有注解。判断用户是否登录了。远程调用verify()，认证用户是否登录
            // 获取服务器上的ip 地址
            String salt = request.getHeader("X-forwarded-for");
            // http://passport.atguigu.com/verify?token=xxx&salt=x
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if ("success".equals(result)){
                // 登录，认证成功！
                // 保存一下userId
                // 开始解密token 获取nickName
                Map map = getUserMapByToken(token);
                // 取出userId
                String userId = (String) map.get("userId");
                // 保存到作用域
                request.setAttribute("userId",userId);
                return true;
            }else {
                // 认证失败！并且 methodAnnotation.autoRedirect()=true; 必须登录
                if (methodAnnotation.autoRedirect()){
                    // 必须登录！跳转到页面！
                    // 京东：https://passport.jd.com/new/login.aspx?ReturnUrl=https%3A%2F%2Fwww.jd.com%2F
                    // 我们：http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F36.html
                    // 先获取到url
                    String requestURL  = request.getRequestURL().toString();
                    System.out.println("requestURL:"+requestURL); // http://item.gmall.com/36.html
                    // 将url 进行转换
                    // http%3A%2F%2Fitem.gmall.com%2F36.html
                    String encodeURL  = URLEncoder.encode(requestURL, "UTF-8");
                    System.out.println("encodeURL："+encodeURL); //  http%3A%2F%2Fitem.gmall.com%2F36.html
                    // http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F36.html
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;
                }
            }
        }

        return true;
    }


    // 解密token获取map数据
    private Map getUserMapByToken(String token) {
        // token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
        // 获取中间部分
        String tokenUserInfo  = StringUtils.substringBetween(token, ".");
        // 将tokenUserInfo 进行base64解码
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        // 解码之后得到byte数组
        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
        // 需要先将decode 转成String
        String mapJson =null;
        try {
            mapJson = new String(decode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 将字符串转换为map 直接返回！
        return JSON.parseObject(mapJson,Map.class);
    }

    // 进入控制器之后，视图渲染之前！
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
    // 视图渲染之后！
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
