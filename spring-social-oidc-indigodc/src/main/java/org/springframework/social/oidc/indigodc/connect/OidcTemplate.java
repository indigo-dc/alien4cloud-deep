package org.springframework.social.oidc.indigodc.connect;

import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.social.oidc.indigodc.api.OidcConfiguration;

public class OidcTemplate extends OAuth2Template {

    private OidcConfiguration configuration;

    public OidcTemplate(OidcConfiguration configuration, String clientId, String clientSecret) {
        super(clientId, clientSecret, configuration.getAuthorizationEndpoint(), configuration.getTokenEndpoint());
        this.configuration = configuration;
    }

    public OidcConfiguration getConfiguration() {
        return configuration;
    }
}
