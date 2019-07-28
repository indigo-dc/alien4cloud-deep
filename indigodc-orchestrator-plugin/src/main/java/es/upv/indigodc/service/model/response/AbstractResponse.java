package es.upv.indigodc.service.model.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractResponse {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected ObjectMapper objectMapper;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected JsonNode rootResponse;

    /** The return code of the call, e.g. 200 for OK */
    protected int code;

    /**
     * Checks the code returned by the server.
     *
     * @return true if the code is between 200 and 299, false otherwise
     */
    public boolean isCodeOk() {
        return code >= 200 && code <= 299;
    }
}
