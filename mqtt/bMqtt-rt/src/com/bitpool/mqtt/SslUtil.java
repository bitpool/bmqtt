package com.bitpool.mqtt;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.StringReader;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;


public class SslUtil {

    public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile, final String password) {
        try {

            /**
             * Add BouncyCastle as a Security Provider
             */
            //SSLSocketFactory context1

            SSLSocketFactory context112 = AccessController.doPrivileged((PrivilegedAction<SSLSocketFactory>) () -> {
                    try {
                        SSLContext context1 = SSLContext.getInstance("TLSv1.2");


                        Security.addProvider(new BouncyCastleProvider());


                        JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");

                        /**
                         * Load Certificate Authority (CA) certificate
                         */
                        // Rather than a file just push in a String and pass tp PEM Parser
                        PEMParser reader = new PEMParser(new StringReader(caCrtFile));
//            PEMParser reader = new PEMParser(new FileReader(caCrtFile));
                        X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
                        reader.close();

                        X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);

                        /**
                         * Load client certificate
                         */
                        reader = new PEMParser(new StringReader(crtFile));
                        //reader = new PEMParser(new FileReader(crtFile));
                        X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
                        reader.close();

                        X509Certificate cert = certificateConverter.getCertificate(certHolder);

                        /**
                         * Load client private key
                         */
                        reader = new PEMParser(new StringReader(keyFile));
                        //reader = new PEMParser(new FileReader(keyFile));
                        Object keyObject = reader.readObject();
                        reader.close();

                        PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
                        JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");

                        KeyPair key;

                        if (keyObject instanceof PEMEncryptedKeyPair) {
                            key = keyConverter.getKeyPair(((PEMEncryptedKeyPair) keyObject).decryptKeyPair(provider));
                        } else {
                            key = keyConverter.getKeyPair((PEMKeyPair) keyObject);
                        }

                        /**
                         * CA certificate is used to authenticate server
                         */
                        KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        caKeyStore.load(null, null);
                        caKeyStore.setCertificateEntry("ca-certificate", caCert);

                        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                                TrustManagerFactory.getDefaultAlgorithm());
                        trustManagerFactory.init(caKeyStore);

                        /**
                         * Client key and certificates are sent to server so it can authenticate the client
                         */
                        KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        clientKeyStore.load(null, null);
                        clientKeyStore.setCertificateEntry("certificate", cert);
                        clientKeyStore.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                                new Certificate[]{cert});

                        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                                KeyManagerFactory.getDefaultAlgorithm());
                        keyManagerFactory.init(clientKeyStore, password.toCharArray());

                        /**
                         * Create SSL socket factory
                         */

                        context1.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                        /**
                         * Return the newly created socket factory object
                         */
                        return context1.getSocketFactory();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                return null;
            });

        } catch (Exception ee) {

        }
        return null;
    }
 }
