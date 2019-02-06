package org.springframework.social.oidc.indigodc.api.impl;

import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.oidc.indigodc.api.IndigoDC;
import org.springframework.social.oidc.indigodc.api.OidcConfiguration;
import org.springframework.social.oidc.indigodc.api.OidcUserProfile;

public class IndigoDCTemplate extends AbstractOAuth2ApiBinding implements IndigoDC {

    private OidcConfiguration configuration;

    public IndigoDCTemplate(OidcConfiguration configuration, String accessToken) {
        super(accessToken);
        this.configuration = configuration;
    }

    public OidcUserProfile getProfile() {
        return getRestTemplate().getForObject(configuration.getUserinfoEndpoint(), OidcUserProfile.class);
    }
}
