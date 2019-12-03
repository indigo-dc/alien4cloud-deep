package es.upv.indigodc.service.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Information about the current page retrieved from the orchestrator. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class Page {

  /** The number of elements on this page. */
  protected int size;
  /** The total number of elements. */
  protected int totalElements;
  /** The total nuber of pages. */
  protected int totalPages;
  /** The current page number. */
  protected int number;
}
