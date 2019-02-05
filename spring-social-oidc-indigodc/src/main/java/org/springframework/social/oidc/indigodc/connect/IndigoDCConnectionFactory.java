package org.springframework.social.oidc.indigodc.connect;

import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.social.oidc.indigodc.api.IndigoDC;

public class IndigoDCConnectionFactory extends OAuth2ConnectionFactory<IndigoDC> {

    public IndigoDCConnectionFactory(String baseUrl, String clientId, String clientSecret) {
        super("indigo-dc", new IndigoDCProvider(baseUrl, clientId, clientSecret), new IndigoDCAdapter());
    }

}
