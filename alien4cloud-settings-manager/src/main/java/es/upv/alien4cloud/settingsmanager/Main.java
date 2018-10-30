package es.upv.alien4cloud.settingsmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.util.Strings;
import org.xml.sax.SAXException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

  public static void main(String[] args) {

      try {
        String volumeDir = getEnvValue("A4C_RUNTIME_DIR");
        String installPath = getEnvValue("A4C_INSTALL_PATH");
        String installDir = getEnvValue("A4C_INSTALL_DIR");
        String installPathDir = installPath + "/" + installDir;
        String configPathDir = installPathDir + "/" + "config";
        String a4cUser = getEnvValue("A4C_USER");

        Integer portHttp = Integer.parseInt(getEnvValue("A4C_PORT_HTTP"));
        Integer portHttps = Integer.parseInt(getEnvValue("A4C_PORT_HTTPS"));
        String adminUser = getEnvValue("A4C_ADMIN_USERNAME");
        String adminPassw = getEnvValue("A4C_ADMIN_PASSWORD");
        String keystorePassword = getEnvValue("A4C_KEY_STORE_PASSWORD");
        String keyPassword = getEnvValue("A4C_KEY_PASSWORD");
        String certsRootPath = getEnvValue("A4C_CERTS_ROOT_PATH");
        String caCertFile = getEnvValue("A4C_PEM_CERT_FILE");
        String caKeyFile = getEnvValue("A4C_PEM_KEY_FILE");
        boolean enableSsl = Boolean.parseBoolean(getEnvValue("A4C_ENABLE_SSL"));
        boolean resetConfig = Boolean.parseBoolean(getEnvValue("A4C_RESET_CONFIG"));
        boolean executeMethod = true;    
      
        File volumeDirFile = new File(volumeDir);
        if (Files.notExists(volumeDirFile.toPath(), LinkOption.NOFOLLOW_LINKS)) {
      	  log.info("Creating A4C_RUNTIME_DIR dir on " + volumeDir);
      	  volumeDirFile.mkdirs();
        } /*else {    	  
      	  if (volumeDirFile.list().length > 0) {
      		 if (resetConfig) {
      			 volumeDirFile.delete();
      			 volumeDirFile.mkdirs();
      		 } else
      			 executeMethod = false;
      	  }
        }*/
      
      if (executeMethod) {
	      ConfigManager configManager;
          try {
            log.info("Try editing conf at " + configPathDir + "/alien4cloud-config.yml");
            configManager = new ConfigManager(volumeDir, installPathDir);
            configManager.setAdminUserPassw(adminUser, adminPassw);
            configManager.setDirectoriesAlien(volumeDir);
            if (enableSsl) {
              try {
                  configManager.enableSsl(
                      keystorePassword,
                      keyPassword,
                      certsRootPath + "/" + caCertFile,
                      certsRootPath + "/" + caKeyFile);
                  configManager.setPort(portHttps);
                  log.info("SSL successfully enabled");
                } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException e) {
                  e.printStackTrace();
                  configManager.disableSsl();
                  configManager.setPort(portHttp);
                  log.info("Unable to create the SSL store; use HTTP instead");
                }
            } else {
                configManager.disableSsl();
                configManager.setPort(portHttp);
                
            }
            configManager.writeConfig(configPathDir + "/alien4cloud-config.yml");
            log.info("Successfully wrote a4c conf at " + configPathDir + "/alien4cloud-config.yml");
          } catch (IOException e1) {
            e1.printStackTrace();
          }
	
	      ElasticSearchManager esm;
          try {
            log.info("Try editing elastic search conf at " + configPathDir + "/elasticsearch.yml");
            esm = new ElasticSearchManager();
            esm.setPath(volumeDir);
            esm.writeConfig(configPathDir + "/elasticsearch.yml");
            log.info("Successfully wrote elastic search conf at " + configPathDir + "/elasticsearch.yml");
          } catch (IOException e) {
            e.printStackTrace();
          }
	
	
	      Log4jManager logm;
          try {
            log.info("Try editing log4j conf at " + configPathDir + "/log4j2.yml");
            logm = new Log4jManager();
            logm.setFullPathLogs(volumeDir);
            logm.writeConfig(configPathDir + "/log4j2.xml");
            log.info("Successfully wrote log4j conf at " + configPathDir + "/log4j2.yml");
          } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
            e.printStackTrace();
          }
	      
	      UserPrincipalLookupService lookupService = FileSystems.getDefault()
	    	        .getUserPrincipalLookupService();
		  UserPrincipal a4cUserP;
          try {
            log.info("Try changing user permissions of the dirs affected by the settings manager");
            a4cUserP = lookupService.lookupPrincipalByName(a4cUser);
            Files.setOwner(volumeDirFile.toPath(), a4cUserP);
            Files.setOwner(Paths.get(configPathDir), a4cUserP);
            log.info("User permissions changed successfully");
          } catch (IOException e) {
            e.printStackTrace();
          }
	    		 
      } else
    	  log.info("App not executed because the volume dir already exists");

      } catch (ValueNotFoundException e) {
        e.printStackTrace();
      }
      
  }

  private static String getEnvValue(String envName) throws ValueNotFoundException {
    String value = System.getenv(envName);
    if (value != null) return value;
    else
      throw new ValueNotFoundException(
          String.format("Variable \"%s\" is not defined in the system", envName));
  }
}
