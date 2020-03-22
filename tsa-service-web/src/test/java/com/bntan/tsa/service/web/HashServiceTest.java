package com.bntan.tsa.service.web;

import com.bntan.tsa.service.web.app.Application;
import com.bntan.tsa.service.web.exceptions.HashException;
import com.bntan.tsa.service.web.service.HashService;
import org.apache.pdfbox.util.Hex;
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

@WebAppConfiguration()
@TestPropertySource("/application-test.properties")
@ContextConfiguration(classes = {Application.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class HashServiceTest {

    @Autowired
    private HashService hashService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void hashSHA1() throws Exception {
        byte[] input = "input".getBytes();
        byte[] output = hashService.hash(input, "SHA-1");
        Assert.assertEquals("140f86aae51ab9e1cda9b4254fe98a74eb54c1a1", Hex.getString(output).toLowerCase());
    }

    @Test
    public void hashSHA256() throws Exception {
        byte[] input = "input".getBytes();
        byte[] output = hashService.hash(input, "SHA-256");
        Assert.assertEquals("c96c6d5be8d08a12e7b5cdc1b207fa6b2430974c86803d8891675e76fd992c20", Hex.getString(output).toLowerCase());
    }

    @Test
    public void hashSHA384() throws Exception {
        byte[] input = "input".getBytes();
        byte[] output = hashService.hash(input, "SHA-384");
        Assert.assertEquals("4fbd200eb6266698f0846c66607c98797e2b9b3af5bf82aa1aa330a0e2b12aba97755e3bc955c9765e9edcc70278ca2c", Hex.getString(output).toLowerCase());
    }

    @Test
    public void hashSHA512() throws Exception {
        byte[] input = "input".getBytes();
        byte[] output = hashService.hash(input, "SHA-512");
        Assert.assertEquals("dc6d6c30f2be9c976d6318c9a534d85e9a1c3f3608321a04b4678ef408124d45d7164f3e562e68c6c0b6c077340a785824017032fddfa924f4cf400e6cbb6adc", Hex.getString(output).toLowerCase());
    }

    @Test
    public void hashMD2() throws Exception {
        byte[] input = "input".getBytes();
        byte[] output = hashService.hash(input, "MD2");
        Assert.assertEquals("18a57ac0576644b9b5abe03557dd5136", Hex.getString(output).toLowerCase());
    }

    @Test
    public void hashMD5() throws Exception {
        byte[] input = "input".getBytes();
        byte[] output = hashService.hash(input, "MD5");
        Assert.assertEquals("a43c1b0aa53a0c908810c06ab1ff3967", Hex.getString(output).toLowerCase());
    }

    @Test
    public void hashUnknownAlgorithm() throws Exception {
        byte[] input = "input".getBytes();
        expectedException.expect(HashException.class);
        expectedException.expectMessage("Error when computing hash: Unknown MessageDigest not available");
        hashService.hash(input, "Unknown");
    }
}
