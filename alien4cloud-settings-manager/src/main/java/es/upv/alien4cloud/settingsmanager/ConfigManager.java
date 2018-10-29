package es.upv.alien4cloud.settingsmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.net.ssl.TrustManagerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import lombok.extern.slf4j.Slf4j;
import okio.Buffer;
import okio.ByteString;

import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.util.io.pem.PemObject;

@Slf4j
public class ConfigManager {
  
  public static final String CONFIG_TEMPLATE_PATH = "config/alien4cloud-config.yml";
  public static final String CERTIFICATE_KEY_ALIAS = "alien4cloud-deep";
  
  private ObjectNode root;
  private String runtimeDir;
  private String installPathDir;
  private ObjectMapper mapper;
  
  public ConfigManager(String runtimeDir, String installPathDir) throws JsonProcessingException, IOException {
    
    this.runtimeDir = runtimeDir;
    this.installPathDir = installPathDir;
    InputStream inConfigTemplate = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_TEMPLATE_PATH);

    mapper = new ObjectMapper(
            new YAMLFactory()
                .disable(Feature.WRITE_DOC_START_MARKER)
                .disable(Feature.SPLIT_LINES)
                .disable(Feature.CANONICAL_OUTPUT));
    root = (ObjectNode) mapper.readTree(inConfigTemplate);
  }
  
  public void setDirectoriesAlien(String fullPath) throws ValueNotFoundException {
	  ObjectNode directories = (ObjectNode) root.findValue("directories");
	    if (directories != null) {
	    	TextNode alien = (TextNode) directories.findValue("alien");
		  	if (alien != null) {
		  		directories.put("alien", fullPath + "/" + alien.textValue());
		    } else 
		      throw new ValueNotFoundException(
		          String.format("Can't find the directories node in the %s template", CONFIG_TEMPLATE_PATH));
	  		  
	    } else 
	      throw new ValueNotFoundException(
	          String.format("Can't find the directories node in the %s template", CONFIG_TEMPLATE_PATH));
  }
  
  public void setPort(int port) throws ValueNotFoundException {
    ObjectNode server = (ObjectNode) root.findValue("server");
    if (server != null) {
      server.put("port", port);
    } else 
      throw new ValueNotFoundException(
          String.format("Can't find the server node in the %s template", CONFIG_TEMPLATE_PATH));
  }
  
  public void disableSsl() throws ValueNotFoundException {
    ObjectNode server = (ObjectNode) root.findValue("server");
    if (server != null) {
      server.remove("ssl");
    } else 
      throw new ValueNotFoundException(
          String.format("Can't find the server node in the %s template", CONFIG_TEMPLATE_PATH));
  }
  
  public void enableSsl(String keystorePassword, String keyPassword,
		  String caCertFullPath, String caKeyFullPath) throws ValueNotFoundException, 
    NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {

    ObjectNode server = (ObjectNode) root.findValue("server");
    if (server != null) {
      ObjectNode ssl = (ObjectNode) server.findValue("ssl");
      if (ssl != null) {
        String keystoreFullPath = this.installPathDir + "/" +  
            ssl.get("key-store").asText();
        boolean created = this.createJavaKeystore(keystoreFullPath, keystorePassword, keyPassword,
            caCertFullPath, caKeyFullPath);
        if (!created) {
          log.error("Unable to store the certificate to enable HTTPS");
          this.disableSsl();
        } else {
          ssl.put("key-store-password", keystorePassword);
          ssl.put("key-password", keyPassword);        
        }
      }
    } else 
      throw new ValueNotFoundException(
          String.format("Can't find the server node in the %s template", CONFIG_TEMPLATE_PATH));
    
  }
  
  public void setAdminUserPassw(String adminUsername, String adminPassw) throws ValueNotFoundException {
    ObjectNode security = (ObjectNode) root.findValue("alien_security");
    if (security != null) {
      ObjectNode admin = (ObjectNode) security.findValue("admin");
      if (admin != null) {
        admin.put("username", adminUsername);
        admin.put("password", adminPassw);
      } else 
        throw new ValueNotFoundException(
            String.format("Can't find the admin node in the %s template", CONFIG_TEMPLATE_PATH));
    } else 
      throw new ValueNotFoundException(
          String.format("Can't find the alien_security node in the %s template", CONFIG_TEMPLATE_PATH));
  }
  
  private boolean createJavaKeystore(String keystoreFullPath, 
      String keystorePassword, String keyPassword,
      String caCertFullPath, String caKeyFullPath) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
      boolean result = true;
    
//    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//    TrustManagerFactory trustManagerFactory =
//        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//    
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

    char[] password = keystorePassword.toCharArray();
    ks.load(null, password);

    // Decode the certificates and add 'em to the key store.
//    String cert = this.certToBase64(caCertFullPath);
//    log.debug("Certificate is: " + cert);
//    Buffer certificateBuffer = new Buffer().write(ByteString.decodeBase64(cert));
//    X509Certificate certificate =
//        (X509Certificate)
//            certificateFactory.generateCertificate(certificateBuffer.inputStream());
    X509Certificate cert = getCertificate(caCertFullPath);
    ks.setCertificateEntry(CERTIFICATE_KEY_ALIAS, cert);
    PrivateKey key = getKey(caKeyFullPath, keyPassword);
    if (key != null)
      ks.setKeyEntry(CERTIFICATE_KEY_ALIAS, key, keyPassword.toCharArray(), new Certificate[] {cert});
    else
      result = false;
    //certificateBuffer.close();


    // Store away the keystore.
    FileOutputStream fos = new FileOutputStream(keystoreFullPath);
    ks.store(fos, password);
    fos.close();
    return result;
  }
  
//  private String certToBase64(String pemFullPath) throws IOException, CertificateException {
//    PEMParser r = new PEMParser(new FileReader(pemFullPath));
//    PemObject object = r.readPemObject();
//    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
//    X509CertificateHolder cert = new X509CertificateHolder(object.getContent());
//    
//    return ;
//  }
  
  private X509Certificate getCertificate(String pemFullPath) throws CertificateException, IOException {
	  CertificateFactory fact = CertificateFactory.getInstance("X.509");
	    FileInputStream is = new FileInputStream (pemFullPath);
	    X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
	    is.close();
	    return cer;
  }
  
  private PrivateKey getKey(String caKeyFullPath, String keyPassword) throws CertificateException, IOException {
	  Security.addProvider(new BouncyCastleProvider());

	    PEMParser pemParser = new PEMParser(new InputStreamReader(new FileInputStream(caKeyFullPath)));
	    Object keyPair = pemParser.readObject();
	    PrivateKey privateKey = null;
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
	    if (keyPair instanceof PEMEncryptedKeyPair) {
	        log.info("PEMEncryptedKeyPair format found");
    	    PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) keyPair;
    	    PEMDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder().build(keyPassword.toCharArray());
    	    PEMKeyPair pemKeyPair = encryptedKeyPair.decryptKeyPair(decryptorProvider);
    	    privateKey = converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
	    } else if (keyPair instanceof PEMKeyPair) {
          log.info("PEMKeyPair format found");
	       PEMKeyPair pemKeyPair = (PEMKeyPair) keyPair;
	       privateKey = converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
	    } else if (keyPair instanceof PrivateKeyInfo )  {
          log.info("PrivateKeyInfo format found");
	      PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) keyPair;
	      privateKey = converter.getPrivateKey(privateKeyInfo);
	    } else 
	      log.error(String.format("Key format %s not handled; Please contact the devs", keyPair.getClass().getCanonicalName()));
        pemParser.close();
	    return privateKey;
  }
  
  public void writeConfig(String outFileFullPath) 
      throws JsonGenerationException, JsonMappingException, IOException {
    ObjectWriter ow = mapper.writer();
    ow.writeValue(new File(outFileFullPath), this.root);
  }
  
  //private void rmNode()

}
