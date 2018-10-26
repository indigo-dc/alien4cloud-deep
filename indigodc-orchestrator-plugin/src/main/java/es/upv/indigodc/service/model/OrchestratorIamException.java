package es.upv.indigodc.service.model;

import lombok.Getter;

/**
 * Throw this error when we detected a problem specific to the orchestrator or IAM.
 *
 * @author asalic
 */
public class OrchestratorIamException extends Exception {
  private static final long serialVersionUID = 1L;

  /** Obtained when calling the orchestrator. */
  @Getter
  private int httpCode;
  /** The title of the error, summarizes the error. */
  @Getter
  private String title;
  /** The body of the error, with details about the error. */
  @Getter
  private String message;

  /**
   * Constructs a new exception with parameters.
   *
   * @param httpCode {@link #httpCode}
   * @param title {@link #title}
   * @param message {@link #message}
   */
  public OrchestratorIamException(int httpCode, String title, String message) {
    super(String.format("Orchestrator/IAM error with code \"%d\", title \"%s\", and message \"%s\"",
        httpCode, title, message));
    this.httpCode = httpCode;
    this.title = title;
    this.message = message;
  }
}
