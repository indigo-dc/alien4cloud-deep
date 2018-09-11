package es.upv.indigodc.service.model;

import org.springframework.http.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Holds the response received after a call to the Orchestrator
 * @author asalic
 *
 */
@Getter
@AllArgsConstructor
public class OrchestratorResponse {

  /**
   * The return code of the call, e.g. 200 for OK
   */
  private int code;
  /**
   * One of the calls methods for HTTP, e.g. GET, POST etc. used to get this response
   */
  private HttpMethod callMethod;
  /**
   * The response itself with all its fields
   */
  private StringBuilder response;
}
