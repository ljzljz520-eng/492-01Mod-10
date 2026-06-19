package com.scaffolding.config;

import com.scaffolding.entity.User;
import com.scaffolding.utils.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final String SESSION_USER_KEY = "CURRENT_USER";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute(SESSION_USER_KEY);
            if (userObj instanceof User) {
                User user = (User) userObj;
                user.setPassword(null);
                UserContext.setUser(user);
                return true;
            }
        }

        writeUnauthorized(response);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", "未登录或会话已过期，请重新登录");
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    public static String getSessionUserKey() {
        return SESSION_USER_KEY;
    }
}
