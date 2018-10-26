package es.upv.indigodc.service.model;

import lombok.Getter;

/**
 * Exception when a status from the orchestrator cannot be mapped. to A4C
 *
 * @author asalic
 */
public class StatusNotFoundException extends Exception {

  /** Default. */
  private static final long serialVersionUID = 1L;
  /** The status that cannot be found. */
  @Getter
  protected String status;

  /**
   * Creates an exception with the status.
   *
   * @param status The status that cannot be found
   */
  public StatusNotFoundException(String status) {
    super(String.format("Status \"%s\" not supported yet", status));
    this.status = status;
  }
}
