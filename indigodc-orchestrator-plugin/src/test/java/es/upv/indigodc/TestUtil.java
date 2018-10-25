package es.upv.indigodc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import alien4cloud.security.model.User;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.service.OrchestratorConnectorTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestUtil {
  
	@Disabled("Not testing method")
  public static CloudConfiguration getRealConfiguration(String fileNameResource)
      throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    if (fileNameResource == null)
      fileNameResource = IndigoDcOrchestratorFactory.CLOUD_CONFIGURATION_DEFAULTS_FILE;
    InputStream is =
        IndigoDcOrchestratorFactory.class.getResourceAsStream(fileNameResource);
    return mapper.readValue(is, CloudConfiguration.class);
  }
  
	@Disabled("Not testing method")
  public static User getTestUser()
      throws JsonParseException, JsonMappingException, IOException {
    User u = new User();
    u.setUsername("testuser");
    u.setPassword("password");
    return u;
  }

	@Disabled("Not testing method")
  public static CloudConfiguration getTestConfiguration(String fileNameResource)
      throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    URL url = OrchestratorConnectorTest.class.getClassLoader().getResource(fileNameResource);
    InputStream is = new FileInputStream(url.getPath());
    CloudConfiguration cf = mapper.readValue(is, CloudConfiguration.class);
    return cf;
  }
  
	@Disabled("Not testing method")
  static void setFinalStatic(Field field, Object newValue) throws Exception {
      field.setAccessible(true);        
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      field.set(null, newValue);
  }
  
	@Disabled("Not testing method")
	public static int setAllPrivateFields(Object target, String fieldsName, Object value) 
			throws IllegalArgumentException, IllegalAccessException {
	  Class<?> tmpClass = target.getClass();
	  int count = 0;
	  do {
	      try {
	          Field f = tmpClass.getDeclaredField(fieldsName);
	          f.setAccessible(true);
	          f.set(target, value);
	          count++;
	      } catch (NoSuchFieldException e) {
	    	  log.info(String.format("Class %s has no field %s", tmpClass.getClass().getCanonicalName(), fieldsName));
	      }
          tmpClass = tmpClass.getSuperclass();
	  } while (tmpClass != null);
	  return count;
	}
	@Disabled("Not testing method")
	public static void setPrivateFieldSuperClass(Object target, String fieldsName, Object value) 
			throws IllegalArgumentException, IllegalAccessException {
	  Class<?> tmpClass = target.getClass();
	  do {
	      try {
	          Field f = tmpClass.getDeclaredField(fieldsName);
	          f.setAccessible(true);
	          f.set(target, value);
	    	  log.info(String.format("Field %s found in class %s", fieldsName, tmpClass.getClass().getCanonicalName()));
	          return;
	      } catch (NoSuchFieldException e) {
	    	  log.info(String.format("Class %s has no field %s", tmpClass.getClass().getCanonicalName(), fieldsName));
	      }
          tmpClass = tmpClass.getSuperclass();
	  } while (tmpClass != null);
	}
  
  
	@Disabled("Not testing method")
  public static void setPrivateField(Object target, String fieldName, Object value){
      try{
          Field privateField = target.getClass().getDeclaredField(fieldName);
          privateField.setAccessible(true);
          privateField.set(target, value);
      }catch(Exception e){
          throw new RuntimeException(e);
      }
  }
//  
//  public static List<Field> getField(Class<?> clazz, String fieldName) {
//	    Class<?> tmpClass = clazz;
//	    List<Field> result = new ArrayList<Field>();
//	    do {
//	        try {
//	            Field f = tmpClass.getDeclaredField(fieldName);
//	            result.add(f);
//	        } catch (NoSuchFieldException e) {
//	            tmpClass = tmpClass.getSuperclass();
//	        }
//	    } while (tmpClass != null);
//	    if (result.isEmpty())
//		    throw new RuntimeException("Field '" + fieldName
//		            + "' not found on class " + clazz);
//	    else
//	    	return result;
//	}


}
