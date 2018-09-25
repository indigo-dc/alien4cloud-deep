package es.upv.indigodc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class TestServer {

  protected Server server;
  protected ServerConnector connectorHttp;
  protected ServerConnector connectorHttps;
  protected ServletContextHandler context;
  protected int port;

  /**
   * Wrapper class for a embedded jetty server. Use for testing only, and in a security reliable
   * environment
   *
   * @param port What unsecure port to use
   * @param securePort What secure port to uses
   * @param keystorePath Points to a testing keystore it must use the pkcs12 format
   * @param keystorePass The password for the keystore
   * @param managerPass The password for the key manager
   * @throws FileNotFoundException Thrown when the keystore cannot be opened
   */
  public TestServer(
      int port, int securePort, String keystorePath, String keystorePass, String managerPass)
      throws FileNotFoundException {
    this.port = port;
    server = new Server(port);

    File keystoreFile = new File(keystorePath);
    if (!keystoreFile.exists()) throw new FileNotFoundException(keystoreFile.getAbsolutePath());

    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
    sslContextFactory.setKeyStorePassword(keystorePass);
    sslContextFactory.setKeyManagerPassword(managerPass);

    HttpConfiguration http_config = new HttpConfiguration();
    http_config.setSecureScheme("https");
    http_config.setSecurePort(securePort);
    http_config.setOutputBufferSize(32768);
    connectorHttp = new ServerConnector(server, new HttpConnectionFactory(http_config));
    connectorHttp.setPort(port);
    connectorHttp.setIdleTimeout(30000);

    HttpConfiguration https_config = new HttpConfiguration(http_config);
    SecureRequestCustomizer src = new SecureRequestCustomizer();
    src.setStsMaxAge(2000);
    src.setStsIncludeSubDomains(true);
    https_config.addCustomizer(src);

    // HTTPS connector
    // We create a second ServerConnector, passing in the http configuration
    // we just made along with the previously created ssl context factory.
    // Next we set the port and a longer idle timeout.
    connectorHttps =
        new ServerConnector(
            server,
            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(https_config));
    connectorHttps.setPort(securePort);
    connectorHttps.setIdleTimeout(500000);
    server.setConnectors(new Connector[] {connectorHttp, connectorHttps});
  }

  public void start(Map<String, TestBlockingServlet> servlets) throws Exception {
    if (server.isStarted()) server.stop();
    context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);

    for (Map.Entry<String, TestBlockingServlet> s : servlets.entrySet()) {
      context.addServlet(new ServletHolder(s.getValue()), s.getKey());
    }
    server.start();
  }

  public int getPort() throws Error {
    if (connectorHttp == null)
      // If the programmer hasn't initialized this class by calling start, then the whole test
      // should be terminated
      throw new Error("Please init the server first");
    else {
      return connectorHttp.getLocalPort();
    }
  }

  public int getSecurePort() throws Error {
    if (connectorHttps == null)
      // If the programmer hasn't initialized this class by calling start, then the whole test
      // should be terminated
      throw new Error("Please init the server first");
    else {
      return connectorHttps.getLocalPort();
    }
  }

  public boolean isStarted() {
    return server.isStarted();
  }

  public void stop() throws Exception {
    server.stop();
    context.destroy();
    server.setHandler(null);
  }
}
