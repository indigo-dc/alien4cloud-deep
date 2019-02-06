package org.springframework.social.oidc.indigodc.api;

import org.springframework.social.ApiBinding;

public interface IndigoDC extends ApiBinding {

    OidcUserProfile getProfile();
}