package com.bntan.tsa.service.web;

import com.bntan.tsa.service.web.app.Application;
import com.bntan.tsa.service.web.service.IOService;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@WebAppConfiguration()
@TestPropertySource("/application-test.properties")
@ContextConfiguration(classes = {Application.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class IOServiceTest {

    @Autowired
    private IOService ioService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void readInvalidBase64() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Input cannot be base 64 decoded: Last unit does not have enough valid bits");
        ioService.read("input", 1);
    }

    @Test
    public void readInvalidSize() throws Exception {
        String input = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf")));
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Input exceeds max size: 1 mo");
        ioService.read(input, 1);
    }

    @Test
    public void readValid() throws Exception {
        String input = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf")));
        byte[] out = ioService.read(input, 20);
        Assert.assertArrayEquals(FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf")), out);
    }

    @Test
    public void write() throws Exception {
        byte[] input = FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf"));
        String output = ioService.write(input);
        Assert.assertEquals(Base64.getEncoder().encodeToString(input), output);
    }

}
