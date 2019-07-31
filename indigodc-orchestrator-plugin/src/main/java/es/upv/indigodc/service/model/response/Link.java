package es.upv.indigodc.service.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A link container.
 */
@Getter
@Setter
@JsonIgnoreProperties
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    /**
     * The rel field.
     */
    protected String rel;
    /**
     * The href.
     */
    protected String href;
}
