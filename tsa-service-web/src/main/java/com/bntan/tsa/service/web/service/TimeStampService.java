package com.bntan.tsa.service.web.service;

import com.bntan.tsa.service.web.client.TSAClient;
import com.bntan.tsa.service.web.common.Connection;
import com.bntan.tsa.service.web.exceptions.TimeStampException;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TimeStampService implements SignatureInterface {

    @Autowired
    private HashService hashService;

    private Connection connection;
    private String hashAlgo;

    /**
     * Constructor
     *
     * @param connection Connection to TSA parameters
     * @param hashAlgo   Algorithm to use (MD2, MD5, SHA-1, SHA-256, SHA-512...)
     */

    public TimeStampService(Connection connection, String hashAlgo) {
        this.connection = connection;
        this.hashAlgo = hashAlgo;
    }

    /**
     * Timestamp a PDF by calling a TSA according to RFC-3161
     *
     * @param in PDF to timestamp
     * @return Timestamped PDF
     * @throws TimeStampException
     */
    public byte[] createTimeStamp(byte[] in) throws TimeStampException {
        try {
            byte[] out;
            try (PDDocument doc1 = PDDocument.load(in); ByteArrayOutputStream baos1 = new ByteArrayOutputStream()) {
                createTimeStamp(doc1, baos1);
                try (PDDocument doc2 = PDDocument.load(baos1.toByteArray()); ByteArrayOutputStream baos2 = new ByteArrayOutputStream()) {
                    doc2.saveIncremental(baos2);
                    out = baos2.toByteArray();
                }
            }
            return out;
        } catch (Exception ex) {
            throw new TimeStampException("Error when timestamping PDF: " + ex.getMessage(), ex);
        }
    }

    private void createTimeStamp(PDDocument document, OutputStream output) throws IOException {
        PDSignature signature = new PDSignature();
        signature.setType(COSName.DOC_TIME_STAMP);
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(COSName.getPDFName("ETSI.RFC3161"));
        document.addSignature(signature, this);
        document.saveIncremental(output);
    }

    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
            byte[] hash = hashService.hash(IOUtils.toByteArray(content), hashAlgo);
            TSAClient client = new TSAClient(connection);
            return client.getTimeStampToken(hash, hashAlgo);
        } catch (Exception ex) {
            throw new IOException("Error when calling timestamp server: " + ex.getMessage(), ex);
        }
    }
}