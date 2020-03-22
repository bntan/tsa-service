package com.bntan.tsa.service.web.service;

import com.bntan.tsa.service.web.exceptions.HashException;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public class HashService {

    /**
     * Hash the content given the algorithm
     *
     * @param content   Content to hash
     * @param algorithm Algorithm to use (MD2, MD5, SHA-1, SHA-256, SHA-512...)
     * @return The content hashed given the algorithm
     * @throws HashException
     */
    public byte[] hash(byte[] content, String algorithm) throws HashException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            return digest.digest(content);
        } catch (Exception ex) {
            throw new HashException("Error when computing hash: " + ex.getMessage(), ex);
        }
    }
}
