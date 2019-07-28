package es.upv.indigodc.service.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    protected String ref;
    protected String href;
}
