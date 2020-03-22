package com.bntan.tsa.service.web.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
public class IOService {

    /**
     * Read input, base 64 decode it, and check its size
     *
     * @param in      Input in string
     * @param maxSize Max size in mo
     * @return Decoded input in bytes
     * @throws IOException
     */
    public byte[] read(String in, int maxSize) throws IOException {
        byte[] out = null;
        try {
            out = Base64.getDecoder().decode(in);
        } catch (Exception ex) {
            throw new IOException("Input cannot be base 64 decoded: " + ex.getMessage());
        }
        if (out.length > maxSize * 1000000) {
            throw new IOException("Input exceeds max size: " + maxSize + " mo");
        }
        return out;
    }

    /**
     * Base 64 encode input
     *
     * @param in Input in bytes
     * @return Encoded input in string
     */
    public String write(byte[] in) {
        return Base64.getEncoder().encodeToString(in);
    }
}
