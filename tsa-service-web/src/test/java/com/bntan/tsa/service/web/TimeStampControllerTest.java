package com.bntan.tsa.service.web;

import com.bntan.tsa.service.gen.model.Request;
import com.bntan.tsa.service.web.app.Application;
import com.bntan.tsa.service.web.controller.TimeStampController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration()
@TestPropertySource("/application-test.properties")
@ContextConfiguration(classes = {Application.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class TimeStampControllerTest {

    private MockMvc mvc;

    @Autowired
    private TimeStampController controller;

    @Before
    public void before() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void timestampAnyOK() throws Exception {
        Request request = new Request();
        request.setId("id");
        request.setInput(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf"))));
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        mvc
                .perform(post("/timestamp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("id"))
                .andExpect(jsonPath("output").isNotEmpty())
                .andExpect(jsonPath("traces[0].operation").value("READ_INPUT"))
                .andExpect(jsonPath("traces[0].status").value("OK"))
                .andExpect(jsonPath("traces[1].operation").value("TIMESTAMP"))
                .andExpect(jsonPath("traces[1].status").value("OK"))
                .andExpect(jsonPath("traces[2].operation").value("WRITE_OUTPUT"))
                .andExpect(jsonPath("traces[2].status").value("OK"))
        ;
    }

    @Test
    public void timestampBalTstampOK() throws Exception {
        Request request = new Request();
        request.setId("id");
        request.setInput(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf"))));
        request.setTimestampServer("BalTstamp");
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        mvc
                .perform(post("/timestamp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("id"))
                .andExpect(jsonPath("output").isNotEmpty())
                .andExpect(jsonPath("traces[0].operation").value("READ_INPUT"))
                .andExpect(jsonPath("traces[0].status").value("OK"))
                .andExpect(jsonPath("traces[1].operation").value("TIMESTAMP"))
                .andExpect(jsonPath("traces[1].status").value("OK"))
                .andExpect(jsonPath("traces[1].source").value("BalTstamp"))
                .andExpect(jsonPath("traces[2].operation").value("WRITE_OUTPUT"))
                .andExpect(jsonPath("traces[2].status").value("OK"))
        ;
    }

    @Test
    public void timestampDigiCertOK() throws Exception {
        Request request = new Request();
        request.setId("id");
        request.setInput(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf"))));
        request.setTimestampServer("DigiCert");
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        mvc
                .perform(post("/timestamp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("id"))
                .andExpect(jsonPath("output").isNotEmpty())
                .andExpect(jsonPath("traces[0].operation").value("READ_INPUT"))
                .andExpect(jsonPath("traces[0].status").value("OK"))
                .andExpect(jsonPath("traces[1].operation").value("TIMESTAMP"))
                .andExpect(jsonPath("traces[1].status").value("OK"))
                .andExpect(jsonPath("traces[1].source").value("DigiCert"))
                .andExpect(jsonPath("traces[2].operation").value("WRITE_OUTPUT"))
                .andExpect(jsonPath("traces[2].status").value("OK"))
        ;
    }

    @Test
    public void timestampUnknownTSA() throws Exception {
        Request request = new Request();
        request.setId("id");
        request.setInput(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File("src/test/resources/CSC_API.pdf"))));
        request.setTimestampServer("unknown");
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        mvc
                .perform(post("/timestamp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
                        .content(content))
                .andExpect(status().is(500))
                .andExpect(jsonPath("id").value("id"))
                .andExpect(jsonPath("output").isEmpty())
                .andExpect(jsonPath("traces[0].operation").value("READ_INPUT"))
                .andExpect(jsonPath("traces[0].status").value("OK"))
        ;
    }

    @Test
    public void timestampInvalidBase64() throws Exception {
        Request request = new Request();
        request.setId("id");
        request.setInput("invalid_input");
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        mvc
                .perform(post("/timestamp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
                        .content(content))
                .andExpect(status().is(500))
                .andExpect(jsonPath("id").value("id"))
                .andExpect(jsonPath("output").isEmpty())
                .andExpect(jsonPath("traces[0].operation").value("READ_INPUT"))
                .andExpect(jsonPath("traces[0].status").value("ERROR"))
        ;
    }

    @Test
    public void timestampInvalidPDF() throws Exception {
        Request request = new Request();
        request.setId("id");
        request.setInput(Base64.getEncoder().encodeToString("invalid_input".getBytes()));
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        mvc
                .perform(post("/timestamp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
                        .content(content))
                .andExpect(status().is(500))
                .andExpect(jsonPath("id").value("id"))
                .andExpect(jsonPath("output").isEmpty())
                .andExpect(jsonPath("traces[0].operation").value("READ_INPUT"))
                .andExpect(jsonPath("traces[0].status").value("OK"))
                .andExpect(jsonPath("traces[1].operation").value("TIMESTAMP"))
                .andExpect(jsonPath("traces[1].status").value("ERROR"))
        ;
    }
}
