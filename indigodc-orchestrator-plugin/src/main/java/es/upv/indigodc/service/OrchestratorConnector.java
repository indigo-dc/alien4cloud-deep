package es.upv.indigodc.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.service.model.OrchestratorResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("orchestrator-connector")
public class OrchestratorConnector {
  
  
  @Data
  public static class AccessToken {
    
    private int life;
    private String accessToken;
    
    public AccessToken(int life, String accessToken) {
      this.life = life;
      this.accessToken = accessToken;
    }

    @Override
    public String toString() {
      return "AccessToken [life=" + life + ", accessToken=" + accessToken + "]";
    }    
    
  }
  
  public static final int POST_CALL = 1;
  public static final int GET_CALL = 2;
  public static final int DELETE_CALL = 3;
  public static final int PUT_CALL = 4;
  
  public static final String WS_PATH_DEPLOYMENTS = "/deployments";
  
  //public static final String STATUS_UNKNOWN = "UNKNOWN";
  public static final int STATUS_UNKNOWN = 1;
  
  private static final Logger LOGGER = Logger.getLogger(OrchestratorConnector.class.getName());
  //private SslContextBuilder sslContextBuilder;
  //private SSLContext sslContext;
 
  public OrchestratorConnector() {    
  }

  public AccessToken obtainAuthTokens(CloudConfiguration cloudConfiguration) throws IOException, NoSuchFieldException  {

    StringBuilder sbuf = new StringBuilder();
    sbuf.append("grant_type=password&");
    sbuf.append("client_id=").append(cloudConfiguration.getClientId()).append("&");
    sbuf.append("client_secret=").append(cloudConfiguration.getClientSecret()).append("&");
    sbuf.append("username=").append(cloudConfiguration.getUser()).append("&");
    sbuf.append("password=").append(cloudConfiguration.getPassword()).append("&");
    sbuf.append("scope=").append(cloudConfiguration.getClientScopes().replaceAll(" ", "%20"));
    URL requestURL = new URL(cloudConfiguration.getTokenEndpoint());
    
    AccessToken at = null;
    SSLContext sslContext = getSSLContext(cloudConfiguration);
    OrchestratorResponse r = restCall(requestURL, sbuf.toString(), null, true, sslContext, 
        GET_CALL);
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map = mapper.readValue(r.getResponse().toString(), new TypeReference<Map<String, String>>(){});
    at = new AccessToken(Integer.parseInt((String)map.get("expires_in")), (String)map.get("access_token"));
    
    return at;
  }
  
  public OrchestratorResponse callGetDeployments(CloudConfiguration cloudConfiguration) throws IOException, NoSuchFieldException  {
    
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration);
    System.out.println(accessToken);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS).append("?");
    sbuf.append("createdBy=").append(URLEncoder.encode(cloudConfiguration.getClientId(), "UTF-8")).
      append(URLEncoder.encode("@", "UTF-8")).
      append(URLEncoder.encode(cloudConfiguration.getIamHost(), "UTF-8"));
    
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());
    
    URL requestURL = new URL(sbuf.toString());

    SSLContext sslContext = getSSLContext(cloudConfiguration);
    return restCall(requestURL, null, headers, isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()), sslContext, 
        GET_CALL);
  }
  
  public OrchestratorResponse callDeploy(CloudConfiguration cloudConfiguration, String yamlTopology) throws IOException, NoSuchFieldException  {
    log.info("call Deploy");
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS);
    
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());
    
    URL requestURL = new URL(sbuf.toString());

    SSLContext sslContext = getSSLContext(cloudConfiguration);
    return restCall(requestURL, yamlTopology, headers, isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()), sslContext, 
        POST_CALL);
  }
  
  public OrchestratorResponse callDeploymentStatus(CloudConfiguration cloudConfiguration, String deploymentId) 
	      throws IOException, NoSuchFieldException  {	    
	    log.info("call deployment status for UUID " + deploymentId);
	    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration);

	    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
	    sbuf.append(WS_PATH_DEPLOYMENTS).append("/").append(deploymentId);
	    
	    Map<String, String> headers = new HashMap<>();
	    headers.put("Accept", "application/json");
	    headers.put("Content-Type", "application/json");
	    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());
	    
	    URL requestURL = new URL(sbuf.toString());

	    SSLContext sslContext = getSSLContext(cloudConfiguration);
	    return restCall(requestURL, null, headers,isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()), sslContext, 
	        GET_CALL);
	  }
  
  public OrchestratorResponse callUndeploy(CloudConfiguration cloudConfiguration, String deploymentId) 
      throws IOException, NoSuchFieldException  {
    
    log.info("call undeploy");
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS).append("/").append(deploymentId);
    
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());
    
    URL requestURL = new URL(sbuf.toString());

    SSLContext sslContext = getSSLContext(cloudConfiguration);
    return restCall(requestURL, null, headers,isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()), sslContext, 
        DELETE_CALL);
  }
  
  public OrchestratorResponse restCall(URL requestURL, String postData, 
      Map<String, String> headers, boolean isSecured, SSLContext sslContext, int requestType) throws IOException, NoSuchFieldException {   

    URLConnection connection  = null;
    if (isSecured) {
      connection = (HttpsURLConnection) requestURL.openConnection();
      ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
    } else
      connection = (HttpURLConnection) requestURL.openConnection();
    connection.setUseCaches(false);
    if (headers != null)
      for (Map.Entry<String, String> entry : headers.entrySet())
        connection.setRequestProperty(entry.getKey(), entry.getValue());
    
    if (postData != null) {

      if (isSecured) 
        ((HttpsURLConnection) connection).setRequestMethod(getRequest(requestType));
      else
        ((HttpURLConnection) connection).setRequestMethod(getRequest(requestType));
      connection.setDoOutput(true);
      byte[] pd = postData.getBytes(StandardCharsets.UTF_8);
      int postDataLength = pd.length;
      connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));

      LOGGER.info("Post Data: " + postData.toString());
      // //Send request
      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.write(pd);
      wr.close();
    } else {
      if (isSecured) 
        ((HttpsURLConnection) connection).setRequestMethod(getRequest(requestType));
      else
        ((HttpURLConnection) connection).setRequestMethod(getRequest(requestType));      
    }
   
    int responseCode;
    String msg;
    if (isSecured) {
      responseCode = ((HttpsURLConnection) connection).getResponseCode();
      msg = ((HttpsURLConnection) connection).getResponseMessage();
    } else {
      responseCode = ((HttpURLConnection) connection).getResponseCode();
      msg = ((HttpURLConnection) connection).getResponseMessage();
    }
    
     LOGGER.info("Response code: " + responseCode);
     LOGGER.info("Response msg: " + msg);
    // Get Response
    InputStream is = connection.getInputStream();
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
      response.append('\r');
    }
    rd.close();
    
    return new OrchestratorResponse(responseCode, response);
  }
  
  public static String getUUIDTopologyDeployment(OrchestratorResponse response) throws JsonProcessingException, IOException {
	  ObjectMapper objectMapper = new ObjectMapper();
	  objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
	  JsonNode root = objectMapper.readTree(response.getResponse().toString());
	  List<JsonNode> vals = root.findValues("uuid");
	  if (vals.size() > 0)
	    return vals.get(0).asText();
	  else
		  throw new NoSuchElementException("The response for deployment doesn't contain an uuid field");
  }
  
  public static String getStatusTopologyDeployment(OrchestratorResponse response) throws JsonProcessingException, IOException {
	  ObjectMapper objectMapper = new ObjectMapper();
	  objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
	  JsonNode root = objectMapper.readTree(response.getResponse().toString());
	  List<JsonNode> vals = root.findValues("status");
	  if (vals.size() > 0)
	    return vals.get(0).asText();
	  else
		  throw new NoSuchElementException("The response for deployment doesn't contain an uuid field");
  }
  
  private SSLContext getSSLContext(CloudConfiguration cloudConfiguration) {

    SslContextBuilder sslContextBuilder = new SslContextBuilder();
    sslContextBuilder.addCertificate(cloudConfiguration.getIamHostCert());
    sslContextBuilder.addCertificate(cloudConfiguration.getOrchestratorEndpointCert());
    sslContextBuilder.addCertificate(cloudConfiguration.getTokenEndpointCert());
    return sslContextBuilder.build();
  }
  
  private String getRequest(int request) throws NoSuchFieldException {
    switch(request) {
    case POST_CALL: return "POST";
    case GET_CALL: return "GET";
    case PUT_CALL: return "PUT";
    case DELETE_CALL: return "DELETE";
    default: throw new NoSuchFieldException("Unable to map to a JAVA request the request with type " + request);
    }
  }
  
  private boolean isUrlSecured(String url) {
    return url.toLowerCase().startsWith("https:");
  }
  

}
