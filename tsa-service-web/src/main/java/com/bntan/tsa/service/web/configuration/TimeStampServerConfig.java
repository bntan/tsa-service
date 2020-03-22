package com.bntan.tsa.service.web.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "timestamp.server")
public class TimeStampServerConfig {

    private List<TimeStampServerSource> source;

    public List<TimeStampServerSource> getSource() {
        return source;
    }

    public void setSource(List<TimeStampServerSource> source) {
        this.source = source;
    }

    @Getter
    @Setter
    public static class TimeStampServerSource {
        private String name;
        private String url;
        private String username;
        private String password;
        private String keystore;
        private String keystorePassword;
        private String truststore;
        private String hashAlgo;
    }
}
