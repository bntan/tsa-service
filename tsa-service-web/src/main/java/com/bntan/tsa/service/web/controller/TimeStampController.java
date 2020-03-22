package com.bntan.tsa.service.web.controller;

import com.bntan.tsa.service.gen.api.TimestampApiController;
import com.bntan.tsa.service.gen.model.Request;
import com.bntan.tsa.service.gen.model.Response;
import com.bntan.tsa.service.gen.model.Traces;
import com.bntan.tsa.service.web.common.Connection;
import com.bntan.tsa.service.web.configuration.ApplicationConfig;
import com.bntan.tsa.service.web.configuration.ProxyConfig;
import com.bntan.tsa.service.web.configuration.TimeStampServerConfig;
import com.bntan.tsa.service.web.service.IOService;
import com.bntan.tsa.service.web.service.TimeStampService;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class TimeStampController extends TimestampApiController {

    private static final Logger LOG = LoggerFactory.getLogger(TimeStampController.class);

    private static final String READ_INPUT_ERROR = "Error when reading input";
    private static final String TIMESTAMP_ERROR = "Error when timestamping PDF";

    @Autowired
    private IOService ioService;

    @Autowired
    private ProxyConfig proxyConfig;

    @Autowired
    private TimeStampServerConfig tssConfig;

    @Value("${server.input.maxsize}")
    private int maxSize;

    @Override
    public ResponseEntity<Response> timestamp(@ApiParam(value = "The timestamp request", required = true) @Valid @RequestBody Request request) {
        LOG.debug("Start transaction with ID {}", request.getId());

        Response response = new Response();
        response.setId(request.getId());

        byte[] in = null;
        byte[] out = null;
        long start;
        long end;
        int duration;

        // Read input
        start = System.currentTimeMillis();
        try {
            in = ioService.read(request.getInput(), maxSize);
            end = System.currentTimeMillis();
            duration = ((Long) (end - start)).intValue();
            addTraces(response, Traces.OperationEnum.READ_INPUT, null, Traces.StatusEnum.OK, null, duration);
        } catch (Exception ex) {
            LOG.error(READ_INPUT_ERROR, ex);
            response.setErrorMessage(READ_INPUT_ERROR);
            end = System.currentTimeMillis();
            duration = ((Long) (end - start)).intValue();
            addTraces(response, Traces.OperationEnum.READ_INPUT, null, Traces.StatusEnum.ERROR, ex.getMessage(), duration);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Timestamp PDF
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        for (TimeStampServerConfig.TimeStampServerSource source : tssConfig.getSource()) {
            if (request.getTimestampServer() == null || request.getTimestampServer().isEmpty() || request.getTimestampServer().equals(source.getName())) {
                start = System.currentTimeMillis();
                try {
                    Connection connection = new Connection(source.getUrl(), source.getUsername(), source.getPassword(), source.getKeystore(), source.getKeystorePassword(), source.getTruststore(), proxyConfig.getProxy());
                    TimeStampService service = (TimeStampService) context.getBean("timeStampService", connection, source.getHashAlgo());
                    out = service.createTimeStamp(in);
                    end = System.currentTimeMillis();
                    duration = ((Long) (end - start)).intValue();
                    addTraces(response, Traces.OperationEnum.TIMESTAMP, source.getName(), Traces.StatusEnum.OK, null, duration);
                    break;
                } catch (Exception ex) {
                    LOG.error(TIMESTAMP_ERROR, ex);
                    end = System.currentTimeMillis();
                    duration = ((Long) (end - start)).intValue();
                    addTraces(response, Traces.OperationEnum.TIMESTAMP, source.getName(), Traces.StatusEnum.ERROR, ex.getMessage(), duration);
                }
            }
        }
        context.close();
        if (out == null) {
            LOG.error(TIMESTAMP_ERROR);
            response.setErrorMessage(TIMESTAMP_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        start = System.currentTimeMillis();
        response.setOutput(ioService.write(out));
        end = System.currentTimeMillis();
        duration = ((Long) (end - start)).intValue();
        addTraces(response, Traces.OperationEnum.WRITE_OUTPUT, null, Traces.StatusEnum.OK, null, duration);

        LOG.debug("Start transaction with ID {}", request.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void addTraces(Response response, Traces.OperationEnum op, String source, Traces.StatusEnum status, String message, Integer duration) {
        Traces traces = new Traces();
        traces.setOperation(op);
        traces.setSource(source);
        traces.setStatus(status);
        traces.setMessage(message);
        traces.setDuration(duration);
        response.addTracesItem(traces);
    }
}
