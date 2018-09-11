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

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.service.model.OrchestratorIAMException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Manage and do the calls to the REST API exposed by the IndigoDC Orchestrator; Connect to the
 * authorization system to obtain a token for the calls to the IndigoDC Orchestrator
 *
 * @author asalic
 */
@Slf4j
@Service("orchestrator-connector")
public class OrchestratorConnector {

  /**
   * Store the token information received after a successful call to the user authorization service
   * from DEEP
   *
   * @author asalic
   */
  @Data
  public static class AccessToken {

    /** The life of the token */
    private int life;
    /** The actual access token */
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

  /** Web service path for deployments operations; It is appended to the orchestrator endpoint */
  public static final String WS_PATH_DEPLOYMENTS = "/deployments";

  // public static final int STATUS_UNKNOWN = 1;

  private static final Logger LOGGER = Logger.getLogger(OrchestratorConnector.class.getName());

  /**
   * Get the token used to access the Orchestrator
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @return the information received from the authorization service
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws OrchestratorIAMException
   */
  public AccessToken obtainAuthTokens(CloudConfiguration cloudConfiguration, String userName, String userPassword)
      throws IOException, NoSuchFieldException, OrchestratorIAMException {

    StringBuilder sbuf = new StringBuilder();
    sbuf.append("grant_type=password&");
    sbuf.append("client_id=").append(cloudConfiguration.getClientId()).append("&");
    sbuf.append("client_secret=").append(cloudConfiguration.getClientSecret()).append("&");
    sbuf.append("username=").append(userName).append("&");
    sbuf.append("password=").append(userPassword).append("&");
    sbuf.append("scope=").append(cloudConfiguration.getClientScopes().replaceAll(" ", "%20"));
    URL requestURL = new URL(cloudConfiguration.getTokenEndpoint());

    AccessToken at = null;
    SSLContext sslContext = getSSLContext(cloudConfiguration);
    OrchestratorResponse r =
        restCall(requestURL, sbuf.toString(), null, true, sslContext, HttpMethod.GET);
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map =
        mapper.readValue(r.getResponse().toString(), new TypeReference<Map<String, String>>() {});
    at =
        new AccessToken(
            Integer.parseInt((String) map.get("expires_in")), (String) map.get("access_token"));

    return at;
  }

  /**
   * Obtain the list of deployments created by the the user with the client id stored in the cloud
   * configuration for the plugin
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @return The response from the orchestrator
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws OrchestratorIAMException
   */
  public OrchestratorResponse callGetDeployments(CloudConfiguration cloudConfiguration, String userName, String userPassword)
      throws IOException, NoSuchFieldException, OrchestratorIAMException {
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration, userName, userPassword);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS).append("?");
    sbuf.append("createdBy=")
        .append(URLEncoder.encode(cloudConfiguration.getClientId(), "UTF-8"))
        .append(URLEncoder.encode("@", "UTF-8"))
        .append(URLEncoder.encode(cloudConfiguration.getIamHost(), "UTF-8"));

    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());

    URL requestURL = new URL(sbuf.toString());

    SSLContext sslContext = getSSLContext(cloudConfiguration);
    return restCall(
        requestURL,
        null,
        headers,
        isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()),
        sslContext,
        HttpMethod.GET);
  }

  /**
   * Deploy an alien 4 cloud topology It is already adapted to the normative TOSCA supported by the
   * Orchestrator
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @param yamlTopology The actual topology accepted by the orchestrator. It is a string formated
   *     and packed for the orchestrator e.g. new lines are replaced with their representation of
   *     '\n'.
   * @return The orchestrator REST response to this call
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws OrchestratorIAMException
   */
  public OrchestratorResponse callDeploy(CloudConfiguration cloudConfiguration, String userName, String userPassword, String yamlTopology)
      throws IOException, NoSuchFieldException, OrchestratorIAMException {
    log.info("call Deploy");
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration, userName, userPassword);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS);

    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());

    URL requestURL = new URL(sbuf.toString());

    SSLContext sslContext = getSSLContext(cloudConfiguration);
    return restCall(
        requestURL,
        yamlTopology,
        headers,
        isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()),
        sslContext,
        HttpMethod.POST);
  }

  /**
   * Get the status of a deployment with a given deployment ID
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @param deploymentId The id of the deployment we need the information for
   * @return The orchestrator REST response to this call
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws OrchestratorIAMException
   */
  public OrchestratorResponse callDeploymentStatus(
      CloudConfiguration cloudConfiguration, String userName, String userPassword, String deploymentId)
      throws IOException, NoSuchFieldException, OrchestratorIAMException {
    log.info("call deployment status for UUID " + deploymentId);
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration, userName, userPassword);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS).append("/").append(deploymentId);

    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());

    URL requestURL = new URL(sbuf.toString());

    SSLContext sslContext = getSSLContext(cloudConfiguration);
    return restCall(
        requestURL,
        null,
        headers,
        isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()),
        sslContext,
        HttpMethod.GET);
  }

  /**
   * Invoke the undeploy REST API for a given deployment ID
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @param deploymentId The id of the deployment we need to undeploy
   * @return The orchestrator REST response to this call
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws OrchestratorIAMException
   */
  public OrchestratorResponse callUndeploy(
      CloudConfiguration cloudConfiguration, String userName, String userPassword, String deploymentId)
      throws IOException, NoSuchFieldException, OrchestratorIAMException {

    log.info("call undeploy");
    AccessToken accessToken = this.obtainAuthTokens(cloudConfiguration, userName, userPassword);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getOrchestratorEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS).append("/").append(deploymentId);

    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + accessToken.getAccessToken());

    URL requestURL = new URL(sbuf.toString());

    SSLContext sslContext = getSSLContext(cloudConfiguration);
    return restCall(
        requestURL,
        null,
        headers,
        isUrlSecured(cloudConfiguration.getOrchestratorEndpoint()),
        sslContext,
        HttpMethod.DELETE);
  }

  /**
   * General method that actually performs the calls of the DEEP Orchestrator and authorization
   * endpoints It can connect to both HTTPS and HTTP servers
   *
   * @param requestURL The endpoint that handles the call
   * @param postData Data to be sent to the endpoint. It can be null
   * @param headers The headers of the call made on the endpoint
   * @param isSecured HTTPS or HTTP protocols
   * @param sslContext The certificate handler
   * @param requestType one of the following values: OrchestratorConnector.{POST, GET, DELETE, PUT}
   * @return The orchestrator REST response to this call
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws OrchestratorIAMException
   */
  public OrchestratorResponse restCall(
      URL requestURL,
      String postData,
      Map<String, String> headers,
      boolean isSecured,
      SSLContext sslContext,
      HttpMethod requestType)
      throws IOException, NoSuchFieldException, OrchestratorIAMException {

    URLConnection connection = null;
    if (isSecured) {
      connection = (HttpsURLConnection) requestURL.openConnection();
      ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
    } else connection = (HttpURLConnection) requestURL.openConnection();
    connection.setUseCaches(false);
    if (headers != null)
      for (Map.Entry<String, String> entry : headers.entrySet())
        connection.setRequestProperty(entry.getKey(), entry.getValue());

    if (postData != null) {

      if (isSecured) ((HttpsURLConnection) connection).setRequestMethod(requestType.name());
      else ((HttpURLConnection) connection).setRequestMethod(requestType.name());
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
      if (isSecured) ((HttpsURLConnection) connection).setRequestMethod(requestType.name());
      else ((HttpURLConnection) connection).setRequestMethod(requestType.name());
    }

    int responseCode;
    String title;
    if (isSecured) {
      responseCode = ((HttpsURLConnection) connection).getResponseCode();
      title = ((HttpsURLConnection) connection).getResponseMessage();
    } else {
      responseCode = ((HttpURLConnection) connection).getResponseCode();
      title = ((HttpURLConnection) connection).getResponseMessage();
    }

    LOGGER.info("Response code: " + responseCode);
    LOGGER.info("Response title: " + title);
    // Get Response
    InputStream is;
    if (200 <= responseCode && responseCode <= 299) {
      is = connection.getInputStream();
      StringBuilder response = getResponse(is);
      LOGGER.info("Response content: " + response);
      return new OrchestratorResponse(responseCode, requestType, response);
    } else {
      if (isSecured) is = ((HttpsURLConnection) connection).getErrorStream();
      else is = ((HttpURLConnection) connection).getErrorStream();
      StringBuilder response = getResponse(is);
      LOGGER.info("Response content: " + response);
      throw new OrchestratorIAMException(responseCode, title, response.toString());
    }
  }

  /**
   * Obtain the response from the DEEP endpoint once a successful HTTP/HTTPS connection is
   * established
   *
   * @param is The stream that contains the response from the endpoint
   * @return The full response from the DEEP endpoint
   * @throws IOException
   */
  protected StringBuilder getResponse(InputStream is) throws IOException {
    StringBuilder response = new StringBuilder();
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
      response.append('\r');
    }
    rd.close();
    return response;
  }

  public static String getOrchestratorUUIDDeployment(OrchestratorResponse response)
      throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
    JsonNode root = objectMapper.readTree(response.getResponse().toString());
    List<JsonNode> vals = root.findValues("uuid");
    if (vals.size() > 0) return vals.get(0).asText();
    else
      throw new NoSuchElementException("The response for deployment doesn't contain an uuid field");
  }

  public static String getStatusTopologyDeployment(OrchestratorResponse response)
      throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
    JsonNode root = objectMapper.readTree(response.getResponse().toString());
    List<JsonNode> vals = root.findValues("status");
    if (vals.size() > 0) return vals.get(0).asText();
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
  
  /**
   * Check if the URL is secured
   *
   * @param url The full url, including https:// or http://
   * @return true if if secure, false otherwise
   */
  private boolean isUrlSecured(String url) {
    return url.toLowerCase().startsWith("https:");
  }
}
