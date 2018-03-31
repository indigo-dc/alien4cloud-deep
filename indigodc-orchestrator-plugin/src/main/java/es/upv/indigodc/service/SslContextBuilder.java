package es.upv.indigodc.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import okio.Buffer;
import okio.ByteString;

@Service("ssl-context-builder")
public final class SslContextBuilder {
  private final List<String> certificateBase64s = new ArrayList<String>();

  public SslContextBuilder addCertificate(String certificateBase64) {
    certificateBase64s.add(certificateBase64);
    return this;
  }

  public SSLContext build() {
    try {
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null); // Use a null input stream + password to create an empty key store.

      // Decode the certificates and add 'em to the key store.
      int nextName = 1;
      for (String certificateBase64 : certificateBase64s) {
        Buffer certificateBuffer = new Buffer().write(ByteString.decodeBase64(certificateBase64));
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(
            certificateBuffer.inputStream());
        keyStore.setCertificateEntry(Integer.toString(nextName++), certificate);
      }

      // Create an SSL context that uses these certificates as its trust store.
      trustManagerFactory.init(keyStore);
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
      return sslContext;
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}