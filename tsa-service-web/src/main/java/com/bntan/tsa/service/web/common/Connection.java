package com.bntan.tsa.service.web.common;

import lombok.Getter;
import lombok.Setter;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

@Getter
public class Connection {

    private URL url;
    private String username;
    private String password;
    private String keystore;
    private String keystorePassword;
    private String truststore;
    private Proxy proxy;

    /**
     * Constructor
     *
     * @param url              Service URL
     * @param username         Username used to connect to the service
     * @param password         Password used to connect to the service
     * @param keystore         Keystore (PKCS#12) used to connect to the service
     * @param keystorePassword Keystore (PKCS#12) password used to connect to the service
     * @param truststore       Truststore (JKS) used to connect to the service
     * @param proxy            Proxy to use
     * @throws MalformedURLException
     */
    public Connection(String url, String username, String password, String keystore, String keystorePassword, String truststore, Proxy proxy) throws MalformedURLException {
        this.url = new URL(url);
        this.username = username;
        this.password = password;
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.truststore = truststore;
        this.proxy = (proxy == null) ? Proxy.NO_PROXY : proxy;
    }
}