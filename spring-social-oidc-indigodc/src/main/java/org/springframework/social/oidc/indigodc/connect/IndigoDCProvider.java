package org.springframework.social.oidc.indigodc.connect;

import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.social.oidc.indigodc.api.IndigoDC;
import org.springframework.social.oidc.indigodc.api.impl.IndigoDCTemplate;

public class IndigoDCProvider extends AbstractOAuth2ServiceProvider<IndigoDC> {

    private String providerUrl;

    public IndigoDCProvider(String providerUrl, String clientId, String clientSecret) {
        super(createOAuth2Template(providerUrl, clientId, clientSecret));
        this.providerUrl = providerUrl;
    }

    private static OAuth2Template createOAuth2Template(String providerUrl, String clientId, String clientSecret) {
        OAuth2Template oAuth2Template = new OAuth2Template(clientId, clientSecret, providerUrl + "/authorize", providerUrl + "/token");
        oAuth2Template.setUseParametersForClientAuthentication(true);
        return oAuth2Template;
    }

    public IndigoDC getApi(String accessToken) {
        return new IndigoDCTemplate(providerUrl, accessToken);
    }
}
