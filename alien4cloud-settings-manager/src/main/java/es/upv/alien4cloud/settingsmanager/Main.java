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
      String caCertFile = getEnvValue("A4C_PEM_CA_CERT_FILE");
      String caKeyFile = getEnvValue("A4C_PEM_CA_KEY_FILE");
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
	      log.info("Write a4c conf at " + configPathDir + "/alien4cloud-config.yml");
	      ConfigManager configManager = new ConfigManager(volumeDir, installPathDir);
	      configManager.setAdminUserPassw(adminUser, adminPassw);
	      configManager.setDirectoriesAlien(volumeDir);
	      if (enableSsl) {
		      configManager.setPort(portHttps);
		      configManager.enableSsl(
		          keystorePassword,
		          keyPassword,
		          certsRootPath + "/" + caCertFile,
		          certsRootPath + "/" + caKeyFile);
	      } else
		      configManager.setPort(portHttp); 
	      configManager.writeConfig(configPathDir + "/alien4cloud-config.yml");
	
	      log.info("Write elastic search conf at " + configPathDir + "/elasticsearch.yml");
	      ElasticSearchManager esm = new ElasticSearchManager();
	      esm.setPath(volumeDir);
	      esm.writeConfig(configPathDir + "/elasticsearch.yml");
	
	
	      log.info("Write log4j conf at " + configPathDir + "/log4j2.yml");
	      Log4jManager logm = new Log4jManager();
	      logm.setFullPathLogs(volumeDir);
	      logm.writeConfig(configPathDir + "/log4j2.xml");
	      
	      UserPrincipalLookupService lookupService = FileSystems.getDefault()
	    	        .getUserPrincipalLookupService();
		  UserPrincipal a4cUserP = lookupService.lookupPrincipalByName(a4cUser);
		  Files.setOwner(volumeDirFile.toPath(), a4cUserP);
		  Files.setOwner(Paths.get(configPathDir), a4cUserP);
	    		 
      } else
    	  log.info("App not executed because the volume dir already exists");

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ValueNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (CertificateException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (TransformerException e) {
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
