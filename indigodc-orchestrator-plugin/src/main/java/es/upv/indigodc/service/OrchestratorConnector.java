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

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.upv.indigodc.IndigoDCOrchestrator;
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
  
  //public static final String STATUS_UNKNOWN = "UNKNOWN";
  public static final int STATUS_UNKNOWN = 1;
  
  private static final Logger LOGGER = Logger.getLogger(OrchestratorConnector.class.getName());
  private SslContextBuilder sslContextBuilder;
  private SSLContext sslContext;
 
  public OrchestratorConnector() {
    
    sslContextBuilder = new SslContextBuilder();
    sslContextBuilder.addCertificate("MIIFOTCCBCGgAwIBAgIQDihOdHgl4y/X+WgnoPumYjANBgkqhkiG9w0BAQsFADBk" + 
        "MQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQtSG9sbGFuZDESMBAGA1UEBxMJ" + 
        "QW1zdGVyZGFtMQ8wDQYDVQQKEwZURVJFTkExGDAWBgNVBAMTD1RFUkVOQSBTU0wg" + 
        "Q0EgMzAeFw0xNzEyMTMwMDAwMDBaFw0yMDEyMTcxMjAwMDBaMHwxCzAJBgNVBAYT" + 
        "AklUMREwDwYDVQQHEwhGcmFzY2F0aTEuMCwGA1UEChMlSXN0aXR1dG8gTmF6aW9u" + 
        "YWxlIGRpIEZpc2ljYSBOdWNsZWFyZTELMAkGA1UECxMCQkExHTAbBgNVBAMTFGlh" + 
        "bS5yZWNhcy5iYS5pbmZuLml0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC" + 
        "AQEAs6nUoL9jy29dkKM3v4SLiY7dtC3S5ZkHIP+6fbCz284bxzv7DxM7gme47MuX" + 
        "rrl9tG8RGH5WnK/ST0tj5+KP/CaC5Nf36j1c/e8SlZuOiTsSqmZYXmkDZtGRmzWI" + 
        "cPcOFNVNduHAPd9scpgQkP/+McLi2xV7xKRPlZRpq3ezg7kwWQ12SIjl/W5GgVEf" + 
        "s+qgqX3mqVFC9erBYlUVsdIE2O4T7IdbVGVEOk/Q/RgY3PUGia7GNiRliD5POecF" + 
        "ynTmFCCjlGtlVM4YooPgaAUHELQcmc0c+eTyOoaBtb2644vUAUJK3vIK3tnjy6VL" + 
        "rClDDHt4Un408x+dwEeX7jf2gQIDAQABo4IBzTCCAckwHwYDVR0jBBgwFoAUZ/2I" + 
        "IBQnmMcJ0iUZu+lREWN1UGIwHQYDVR0OBBYEFG8UOkbHg9VHfgPdQ0TcHaokU0H4" + 
        "MB8GA1UdEQQYMBaCFGlhbS5yZWNhcy5iYS5pbmZuLml0MA4GA1UdDwEB/wQEAwIF" + 
        "oDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwawYDVR0fBGQwYjAvoC2g" + 
        "K4YpaHR0cDovL2NybDMuZGlnaWNlcnQuY29tL1RFUkVOQVNTTENBMy5jcmwwL6At" + 
        "oCuGKWh0dHA6Ly9jcmw0LmRpZ2ljZXJ0LmNvbS9URVJFTkFTU0xDQTMuY3JsMEwG" + 
        "A1UdIARFMEMwNwYJYIZIAYb9bAEBMCowKAYIKwYBBQUHAgEWHGh0dHBzOi8vd3d3" + 
        "LmRpZ2ljZXJ0LmNvbS9DUFMwCAYGZ4EMAQICMG4GCCsGAQUFBwEBBGIwYDAkBggr" + 
        "BgEFBQcwAYYYaHR0cDovL29jc3AuZGlnaWNlcnQuY29tMDgGCCsGAQUFBzAChixo" + 
        "dHRwOi8vY2FjZXJ0cy5kaWdpY2VydC5jb20vVEVSRU5BU1NMQ0EzLmNydDAMBgNV" + 
        "HRMBAf8EAjAAMA0GCSqGSIb3DQEBCwUAA4IBAQCCyBPHs2L3/LTrBxFx38cDb2RX" + 
        "UvNrLD3b6MH8wXtdT29mlCJLlPTHP/ZLwB0NI4i+ojR4m1AfGUQx164y66crkdZV" + 
        "PijVgWtyH5PSa1+jf1d3mD+eEyfKLX7htyw1CUx1ubGWT1Hgeq0Ltuej8TxQu42R" + 
        "lofP8t5BChrCHFfkDZm9OqLqXxT2joIAkL3+xmrc1+ghUTU6OWKxOBm6fM7ybhjO" + 
        "0Lfk2K3RRvdNTFtMR6Eaaohtx1wmYEXNjBVSAuMvvjpt/F74gMixiOhPwUJ388hu" + 
        "18FUuwOXw1vALUbHxL91rhBKRfqL8322KWmqdo8goXJeRYT6gcmQlvBmpDtD");
    sslContext = sslContextBuilder.build();
  }

  public AccessToken obtainAuthTokens(CloudConfiguration cloudConfiguration) throws IOException, NoSuchFieldException  {

    StringBuilder sbuf = new StringBuilder();
    sbuf.append("grant_type=password&");
    sbuf.append("client_id=").append(cloudConfiguration.getClientId()).append("&");
    sbuf.append("client_secret=").append(cloudConfiguration.getClientSecret()).append("&");
    sbuf.append("username=").append(cloudConfiguration.getUser()).append("&");
    sbuf.append("password=").append(cloudConfiguration.getPassword()).append("&");
    sbuf.append("scope=").append(cloudConfiguration.getClientScopes());
    URL requestURL = new URL(cloudConfiguration.getTokenEndpoint());
    
    AccessToken at = null;
    
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
    sbuf.append("/deployments").append("?");
    sbuf.append("createdBy=").append(URLEncoder.encode(cloudConfiguration.getClientId(), "UTF-8")).
      append(URLEncoder.encode("@", "UTF-8")).
      append(URLEncoder.encode(cloudConfiguration.getIamHost(), "UTF-8"));
    
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());
    
    URL requestURL = new URL(sbuf.toString());
    
    return restCall(requestURL, null, headers, isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()), sslContext, 
        GET_CALL);
  }
  
  public OrchestratorResponse callDeploy(CloudConfiguration cloudConfiguration, String yamlTopology) throws IOException, NoSuchFieldException  {
    log.info("call Deploy");
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append("/deployments");
    
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());
    
    URL requestURL = new URL(sbuf.toString());
    
    return restCall(requestURL, yamlTopology, headers, isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()), sslContext, 
        POST_CALL);
  }
  
  public OrchestratorResponse callDeploymentStatus(CloudConfiguration cloudConfiguration, String deploymentId) 
	      throws IOException, NoSuchFieldException  {	    
	    log.info("call deployment status for UUID " + deploymentId);
	    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration);

	    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
	    sbuf.append("/deployments").append("/").append(deploymentId);
	    
	    Map<String, String> headers = new HashMap<>();
	    headers.put("Accept", "application/json");
	    headers.put("Content-Type", "application/json");
	    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());
	    
	    URL requestURL = new URL(sbuf.toString());
	    
	    return restCall(requestURL, null, headers,isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()), sslContext, 
	        GET_CALL);
	  }
  
  public OrchestratorResponse callUndeploy(CloudConfiguration cloudConfiguration, String deploymentId) 
      throws IOException, NoSuchFieldException  {
    
    log.info("call undeploy");
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append("/deployments").append("/").append(deploymentId);
    
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());
    
    URL requestURL = new URL(sbuf.toString());
    
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
