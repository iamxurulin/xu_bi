package com.rulin.xubibackend.utils;

import java.net.InetAddress;
import javax.servlet.http.HttpServletRequest;

/**
 * NetUtils工具类
 * 用于获取客户端的IP地址
 */
public class NetUtils {
    /**
     * 获取客户端IP地址的方法
     * @param request HttpServletRequest对象
     * @return 返回客户端的IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        // 从x-forwarded-for头中获取IP地址
        String ip = request.getHeader("x-forwarded-for");
        // 如果获取到的IP为空、空字符串或"unknown"，则尝试从Proxy-Client-IP头中获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        // 如果仍然获取不到有效IP，则尝试从WL-Proxy-Client-IP头中获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        // 如果仍然获取不到有效IP，则使用getRemoteAddr()方法获取IP
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            // 如果获取到的IP是本地回环地址127.0.0.1，则尝试获取本机实际IP地址
            if (ip.equals("127.0.0.1")) {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try {
                    // 获取本地主机地址
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    // 捕获并打印异常信息
                    e.printStackTrace();
                }
                // 如果成功获取到本地主机地址，则获取其IP地址
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null) {
            return "127.0.0.1";
        }
        return ip;
    }

}
