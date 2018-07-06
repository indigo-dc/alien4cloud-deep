package es.upv.indigodc.service.model;

import lombok.Getter;

/**
 * Throw this error when we detected a problem specific to the orchestrator or IAM
 *
 * @author asalic
 */
public class OrchestratorIAMException extends Exception {
  private static final long serialVersionUID = 1L;

  @Getter private int httpCode;
  @Getter private String title;
  @Getter private String message;

  public OrchestratorIAMException(int httpCode, String title, String message) {
    super(
        String.format(
            "Orchestrator/IAM error with code \"%d\", title \"%s\", and message \"%s\"",
            httpCode, title, message));
    this.httpCode = httpCode;
    this.title = title;
    this.message = message;
  }
}
