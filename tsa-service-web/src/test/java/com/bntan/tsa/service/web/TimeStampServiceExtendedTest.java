package com.bntan.tsa.service.web;

import com.bntan.tsa.service.web.app.Application;
import com.bntan.tsa.service.web.common.Connection;
import com.bntan.tsa.service.web.configuration.ApplicationConfig;
import com.bntan.tsa.service.web.configuration.ProxyConfig;
import com.bntan.tsa.service.web.configuration.TimeStampServerConfig;
import com.bntan.tsa.service.web.exceptions.TimeStampException;
import com.bntan.tsa.service.web.service.TimeStampService;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;

@WebAppConfiguration()
@TestPropertySource("/application-test-extended.properties")
@ContextConfiguration(classes = {Application.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class TimeStampServiceExtendedTest {

    @Autowired
    private TimeStampServerConfig tssConfig;

    @Autowired
    private ProxyConfig proxyConfig;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void timestampConnectionRefused() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        for (TimeStampServerConfig.TimeStampServerSource source : tssConfig.getSource()) {
            if ("DigiCert".equals(source.getName())) {
                byte[] input = FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf"));
                Connection connection = new Connection(source.getUrl(), source.getUsername(), source.getPassword(), source.getKeystore(), source.getKeystorePassword(), source.getTruststore(), proxyConfig.getProxy());
                TimeStampService service = (TimeStampService) context.getBean("timeStampService", connection, source.getHashAlgo());
                expectedException.expect(TimeStampException.class);
                expectedException.expectMessage("Error when calling timestamp server: Connection refused");
                service.createTimeStamp(input);
            }
        }
        context.close();
    }
}
