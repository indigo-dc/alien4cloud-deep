package org.springframework.social.oidc.indigodc.connect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;
import org.springframework.social.oidc.indigodc.api.IndigoDC;
import org.springframework.social.oidc.indigodc.api.OidcConfiguration;
import org.springframework.social.oidc.indigodc.api.impl.IndigoDCTemplate;
import org.springframework.web.client.RestTemplate;

public class IndigoDCProvider extends AbstractOAuth2ServiceProvider<IndigoDC> {

    private final static Log logger = LogFactory.getLog(IndigoDCProvider.class);

    private OidcConfiguration configuration;

    public IndigoDCProvider(String providerUrl, String clientId, String clientSecret) {
        super(createOidc2Template(providerUrl, clientId, clientSecret));
        configuration = ((OidcTemplate)getOAuthOperations()).getConfiguration();
    }

    private static OidcTemplate createOidc2Template(String providerUrl, String clientId, String clientSecret) {
        RestTemplate tempTemplate = new RestTemplate();
        OidcConfiguration configuration = new OidcConfiguration();
        try {
            configuration = tempTemplate.getForObject(providerUrl+"/.well-known/openid-configuration", OidcConfiguration.class);
        } catch (Exception e) {
            logger.warn("Error getting OIDC issuer configuration", e);
            logger.warn("Setting default values for endpoints");
            configuration.setIssuer(providerUrl);
            configuration.setAuthorizationEndpoint(providerUrl + "/authorize");
            configuration.setTokenEndpoint(providerUrl + "/token");
            configuration.setUserinfoEndpoint(providerUrl + "/userinfo");
        }
        OidcTemplate oAuth2Template = new OidcTemplate(configuration, clientId, clientSecret);
        oAuth2Template.setUseParametersForClientAuthentication(true);
        return oAuth2Template;
    }

    public IndigoDC getApi(String accessToken) {
        return new IndigoDCTemplate(configuration, accessToken);
    }
}
