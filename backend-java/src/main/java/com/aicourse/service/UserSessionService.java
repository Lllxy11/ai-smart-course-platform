package com.aicourse.service;

import com.aicourse.entity.User;
import com.aicourse.entity.UserSession;
import com.aicourse.repository.UserSessionRepository;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户会话服务
 */
@Service
public class UserSessionService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    /**
     * 创建用户会话（在新事务中执行）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserSession createUserSession(User user, HttpServletRequest request) {
        try {
            // 解析用户代理
            String userAgentString = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            
            // 获取客户端IP
            String clientIp = getClientIpAddress(request);
            
            // 生成会话令牌
            String sessionToken = generateSessionToken();
            
            // 将其他会话设为非当前
            userSessionRepository.setOtherSessionsAsNotCurrent(user.getId());
            
            // 创建新会话
            UserSession session = new UserSession();
            session.setUserId(user.getId());
            session.setSessionToken(sessionToken);
            session.setDeviceName(userAgent.getBrowser().getName() + " on " + userAgent.getOperatingSystem().getName());
            session.setBrowser(userAgent.getBrowser().getName() + " " + userAgent.getBrowserVersion());
            session.setOperatingSystem(userAgent.getOperatingSystem().getName());
            session.setIpAddress(clientIp);
            session.setLocation("未知"); // 可以集成IP地理位置API
            session.setUserAgent(userAgentString);
            session.setIsActive(true);
            session.setIsCurrent(true);
            session.setLoginTime(LocalDateTime.now());
            session.setLastActivity(LocalDateTime.now());
            session.setExpiresAt(LocalDateTime.now().plusDays(30));

            return userSessionRepository.save(session);
        } catch (Exception e) {
            // 记录错误但不抛出异常，避免影响主要的登录流程
            System.err.println("Failed to create user session: " + e.getMessage());
            return null;
        }
    }

    /**
     * 异步创建用户会话
     */
    public void createUserSessionAsync(User user, HttpServletRequest request) {
        // 在新线程中执行，避免阻塞主要流程
        new Thread(() -> {
            try {
                createUserSession(user, request);
            } catch (Exception e) {
                System.err.println("Async session creation failed: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 获取登录设备列表
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLoginDevices(User user) {
        List<UserSession> sessions = userSessionRepository.findByUserIdAndIsActive(user.getId(), true);
        
        List<Map<String, Object>> devices = sessions.stream()
                .map(session -> {
                    Map<String, Object> device = new HashMap<>();
                    device.put("id", session.getId());
                    device.put("device_name", session.getDeviceName() != null ? session.getDeviceName() : "未知设备");
                    device.put("browser", session.getBrowser());
                    device.put("operating_system", session.getOperatingSystem());
                    device.put("ip_address", session.getIpAddress());
                    device.put("location", session.getLocation() != null ? session.getLocation() : "未知位置");
                    device.put("last_login", session.getLoginTime());
                    device.put("last_activity", session.getLastActivity());
                    device.put("is_current", session.getIsCurrent());
                    return device;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("devices", devices);
        return result;
    }

    /**
     * 退出指定设备
     */
    @Transactional
    public void logoutDevice(User user, Long deviceId) {
        userSessionRepository.deactivateSession(deviceId, user.getId());
    }

    /**
     * 退出所有设备
     */
    @Transactional
    public void logoutAllDevices(User user) {
        userSessionRepository.deactivateAllUserSessions(user.getId());
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 生成会话令牌
     */
    private String generateSessionToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
} 