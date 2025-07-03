package com.aicourse.security;

import com.aicourse.entity.User;
import com.aicourse.repository.UserRepository;
import com.aicourse.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * JWT请求过滤器
 * 处理每个请求的JWT验证
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain chain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token在header中的格式是 "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtils.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                logger.warn("无法从JWT Token中获取用户名: " + e.getMessage());
            }
        }

        // 验证token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<User> userOptional = userRepository.findByUsername(username);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // 验证token是否有效且用户处于激活状态
                if (jwtUtils.validateToken(jwtToken, user.getId()) && user.getIsActive()) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            user, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到安全上下文中
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳过这些路径的JWT验证
        return path.equals("/") || 
               path.equals("/health") ||
               path.startsWith("/auth/login") || 
               path.startsWith("/auth/register") ||
               path.startsWith("/auth/health") ||
               path.startsWith("/swagger-ui") || 
               path.startsWith("/api-docs") ||
               path.startsWith("/uploads") ||
               "OPTIONS".equals(request.getMethod());
    }
} 