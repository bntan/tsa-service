package com.bntan.tsa.service.web.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

@Configuration
public class ProxyConfig {

    @Value("${proxy.enabled}")
    private boolean enabled;
    @Value("${proxy.host}")
    private String host;
    @Value("${proxy.port}")
    private int port;
    @Value("${proxy.username}")
    private String username;
    @Value("${proxy.password}")
    private String password;

    public Proxy getProxy() {
        if (!enabled) {
            return Proxy.NO_PROXY;
        }
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            Authenticator authenticator = new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return (new PasswordAuthentication(username, password.toCharArray()));
                }
            };
            Authenticator.setDefault(authenticator);
        }
        return proxy;
    }
}
