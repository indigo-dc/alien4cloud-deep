package es.upv.indigodc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.upv.indigodc.service.OrchestratorConnectorTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestBlockingServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  protected String contentType;
  protected int status;
  protected String responseData;
  // protected HttpServletResponse response;

  public static final String CONTENT_TYPE_JSON = "application/json";

  public TestBlockingServlet(String contentType, int status, String responseData) {
    this.contentType = contentType;
    this.status = status;
    this.responseData = responseData;
  }

  //  public TestBlockingServlet(HttpServletResponse response) {
  //    this.response = response;
  //  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType(contentType);
    response.setStatus(status);
    response.getWriter().println(responseData);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType(contentType);
    response.setStatus(status);
    response.getWriter().println(responseData);
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType(contentType);
    response.setStatus(status);
    response.getWriter().println(responseData);
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType(contentType);
    response.setStatus(status);
    response.getWriter().println(responseData);
  }
}
