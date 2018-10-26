package es.upv.indigodc.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import okio.Buffer;
import okio.ByteString;
import org.springframework.stereotype.Service;


/**
 * Creates the context needed to communicate with different servers over HTTPS using custom
 * certificates. Some certificates may not be validaded automatically by the system, therefore we
 * have to add them to the database manually. We can also control what certificates are issued for a
 * certain server These are stored in a {@link es.upv.indigodc.configuration.CloudConfiguration}
 * instance.
 *
 * @author asalic
 */
@Service("ssl-context-builder")
public final class SslContextBuilder {
  private final List<String> certificateBase64s = new ArrayList<String>();

  /**
   * Add a certificate to the current context.
   *
   * @param certificateBase64 The certificate encoded in BASE64
   * @return the instance of the builder that aggregated this certificate
   */
  public SslContextBuilder addCertificate(String certificateBase64) {
    certificateBase64s.add(certificateBase64);
    return this;
  }

  /**
   * Create the context for the certificates added with {@link #addCertificate}.
   *
   * @return the generated context with the custom certificates included
   */
  public SSLContext build() {
    try {
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null); // Use a null input stream + password to create an empty key store.

      // Decode the certificates and add 'em to the key store.
      int nextName = 1;
      for (String certificateBase64 : certificateBase64s) {
        Buffer certificateBuffer = new Buffer().write(ByteString.decodeBase64(certificateBase64));
        X509Certificate certificate = (X509Certificate) certificateFactory
            .generateCertificate(certificateBuffer.inputStream());
        keyStore.setCertificateEntry(Integer.toString(nextName++), certificate);
        certificateBuffer.close();
      }

      // Create an SSL context that uses these certificates as its trust store.
      trustManagerFactory.init(keyStore);
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
      return sslContext;
    } catch (GeneralSecurityException er) {
      throw new RuntimeException(er);
    } catch (IOException er) {
      throw new RuntimeException(er);
    }
  }
}
