package com.bntan.tsa.service.web.client;

import com.bntan.tsa.service.web.common.Connection;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.*;
import org.bouncycastle.util.encoders.Base64;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URLConnection;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class TSAClient {

    private Connection connection;

    /**
     * Constructor
     *
     * @param connection Connection to TSA parameters
     */
    public TSAClient(Connection connection) {
        this.connection = connection;
    }

    /**
     * Timestamp by calling a TSA according to RFC-3161
     *
     * @param hash      Hash to timestamp
     * @param algorithm Algorithm to use to hash the content
     * @return The timestamp token
     * @throws TSPException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws OperatorCreationException
     * @throws UnrecoverableKeyException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public byte[] getTimeStampToken(byte[] hash, String algorithm) throws TSPException, IOException, NoSuchAlgorithmException, CertificateException, OperatorCreationException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        SecureRandom random = new SecureRandom();
        int nonce = random.nextInt();

        TimeStampRequestGenerator generator = new TimeStampRequestGenerator();
        generator.setCertReq(true);
        ASN1ObjectIdentifier oid = getHashObjectIdentifier(algorithm);
        TimeStampRequest request = generator.generate(oid, hash, BigInteger.valueOf(nonce));

        byte[] tsaResponse = getTSAResponse(request.getEncoded());

        TimeStampResponse response = new TimeStampResponse(tsaResponse);
        // Validate response against request (nonce, algorithm, hash value...)
        response.validate(request);

        TimeStampToken token = response.getTimeStampToken();
        if (token == null) {
            throw new TSPException("Response does not include any timestamp token");
        }
        // Validate cert (validity date, correct ExtendedKeyUsage extension...) and signature (signing date, signature cryptographically valid...)
        validateToken(token);

        return token.getEncoded();
    }

    private byte[] getTSAResponse(byte[] request) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        URLConnection connect = connection.getUrl().openConnection(connection.getProxy());
        connect.setDoOutput(true);
        connect.setDoInput(true);
        connect.setRequestProperty("Content-Type", "application/timestamp-query");
        if (connection.getUsername() != null && connection.getPassword() != null && !connection.getUsername().isEmpty() && !connection.getPassword().isEmpty()) {
            String basicAuthn = new String(Base64.encode((connection.getUsername() + ":" + connection.getPassword()).getBytes()));
            connect.setRequestProperty("Authorization", "Basic " + basicAuthn);
        }
        if (connect instanceof HttpsURLConnection) {
            KeyManager[] km = generateKeyManagers();
            TrustManager[] tm = generateTrustManagers();
            if (km.length > 0 || tm.length > 0) {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init((km.length > 0) ? km : null, (tm.length > 0) ? tm : null, new SecureRandom());
                SSLSocketFactory socketFactory = ctx.getSocketFactory();
                ((HttpsURLConnection) connect).setSSLSocketFactory(socketFactory);
            }
        }
        OutputStream output = null;
        try {
            output = connect.getOutputStream();
            output.write(request);
            output.flush();
        } finally {
            IOUtils.closeQuietly(output);
        }

        InputStream input = null;
        byte[] response;
        try {
            input = connect.getInputStream();
            response = IOUtils.toByteArray(input);
        } finally {
            IOUtils.closeQuietly(input);
        }

        return response;
    }

    private ASN1ObjectIdentifier getHashObjectIdentifier(String algorithm) {
        switch (algorithm) {
            case "MD2":
                return new ASN1ObjectIdentifier(PKCSObjectIdentifiers.md2.getId());
            case "MD5":
                return new ASN1ObjectIdentifier(PKCSObjectIdentifiers.md5.getId());
            case "SHA-1":
                return new ASN1ObjectIdentifier(OIWObjectIdentifiers.idSHA1.getId());
            case "SHA-224":
                return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha224.getId());
            case "SHA-256":
                return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha256.getId());
            case "SHA-384":
                return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha384.getId());
            case "SHA-512":
                return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha512.getId());
            default:
                return new ASN1ObjectIdentifier(algorithm);
        }
    }

    private void validateToken(TimeStampToken token) throws TSPException, CertificateException, IOException, OperatorCreationException {
        Collection<X509CertificateHolder> matches = token.getCertificates().getMatches(token.getSID());
        X509CertificateHolder holder = matches.iterator().next();
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);
        SignerInformationVerifier siv = new JcaSimpleSignerInfoVerifierBuilder().setProvider(SecurityProvider.getProvider()).build(cert);
        token.validate(siv);
    }

    private KeyManager[] generateKeyManagers() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        if (connection.getKeystore() != null && !connection.getKeystore().isEmpty() && connection.getKeystorePassword() != null && !connection.getKeystorePassword().isEmpty()) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(connection.getKeystore()), connection.getKeystorePassword().toCharArray());
            KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmFactory.init(ks, connection.getKeystorePassword().toCharArray());
            return kmFactory.getKeyManagers();
        }
        return new KeyManager[0];
    }

    private TrustManager[] generateTrustManagers() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (connection.getTruststore() != null && !connection.getTruststore().isEmpty()) {
            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(new FileInputStream(connection.getTruststore()), null);
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmFactory.init(ts);
            return tmFactory.getTrustManagers();
        }
        return new TrustManager[0];
    }
}