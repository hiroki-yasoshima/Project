package com.example.demo.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionTimeoutInterceptor implements HandlerInterceptor {

    @Override
    // リクエストがコントローラーに到達する前に呼び出される
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            // セッションが無効な場合、ログインページにリダイレクト
            response.sendRedirect(request.getContextPath() + "/login?timeout=true");
         // リクエストを続行しない
            return false;
        }
     // リクエストを続行
        return true;
    }
}