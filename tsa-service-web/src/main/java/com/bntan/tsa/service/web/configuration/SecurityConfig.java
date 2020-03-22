package com.bntan.tsa.service.web.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${server.authn.basic.username}")
    private String username;
    @Value("${server.authn.basic.password}")
    private String password;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().httpBasic();
        }
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authn) throws Exception {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            authn.inMemoryAuthentication().withUser(username).password("{noop}" + password).roles("USER");
        }
    }
}