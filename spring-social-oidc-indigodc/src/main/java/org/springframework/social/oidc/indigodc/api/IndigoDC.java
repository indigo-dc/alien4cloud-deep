package org.springframework.social.oidc.indigodc.api;

import org.springframework.social.ApiBinding;
import org.springframework.social.connect.UserProfile;

public interface IndigoDC extends ApiBinding {

    OidcUserProfile getProfile();

}
