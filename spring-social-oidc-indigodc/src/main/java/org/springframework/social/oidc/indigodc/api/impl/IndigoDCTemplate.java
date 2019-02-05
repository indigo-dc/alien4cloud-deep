package org.springframework.social.oidc.indigodc.api.impl;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.oidc.indigodc.api.IndigoDC;
import org.springframework.social.oidc.indigodc.api.OidcUserProfile;

public class IndigoDCTemplate extends AbstractOAuth2ApiBinding implements IndigoDC {

    private String baseUrl;

    public IndigoDCTemplate(String baseUrl, String accessToken) {
        super(accessToken);
        this.baseUrl = baseUrl;
    }

    public OidcUserProfile getProfile() {
        return getRestTemplate().getForObject(baseUrl+"/userinfo", OidcUserProfile.class);
    }
}
