package org.example.aichatbot.domain.security.service;


import lombok.extern.slf4j.Slf4j;

import org.apache.shiro.web.filter.AccessControlFilter;
import org.example.aichatbot.domain.security.model.vo.JwtToken;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtFilter extends AccessControlFilter {
    /**
     * isAccessAllowed determines whether a valid JwtToken is provided.
     * Here, we directly return false, allowing it to proceed to the onAccessDenied method.
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        return false;
    }

    /**
     * Returns true if the login is successful.
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // If you set the token in the header, you can retrieve it like this: request.getHeader("Authorization");
        JwtToken jwtToken = new JwtToken(request.getParameter("token"));
        try {
            // Authentication
            getSubject(servletRequest, servletResponse).login(jwtToken);
            return true;
        } catch (Exception e) {
            log.error("Authentication failed", e);
            onLoginFail(servletResponse);
            return false;
        }
    }

    /**
     * When authentication fails, the default response is a 401 status code.
     */
    private void onLoginFail(ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("Auth Err!");
    }

}