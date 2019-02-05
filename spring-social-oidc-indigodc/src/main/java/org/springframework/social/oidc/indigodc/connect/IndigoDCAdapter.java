package org.springframework.social.oidc.indigodc.connect;

import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;
import org.springframework.social.oidc.indigodc.api.IndigoDC;
import org.springframework.social.oidc.indigodc.api.OidcUserProfile;

public class IndigoDCAdapter implements ApiAdapter<IndigoDC> {

    public boolean test(IndigoDC api) {
        return api.isAuthorized() && api.getProfile() != null;
    }

    public void setConnectionValues(IndigoDC api, ConnectionValues values) {
        OidcUserProfile profile = api.getProfile();
        values.setProviderUserId(profile.getSub());
        values.setDisplayName(profile.getGivenName() + " " + profile.getFamilyName());
    }

    public UserProfile fetchUserProfile(IndigoDC api) {
        OidcUserProfile profile = api.getProfile();
        return new UserProfileBuilder().setId(profile.getSub())
                .setUsername(profile.getPreferredUsername())
                .setEmail(profile.getEmail())
                .setFirstName(profile.getGivenName())
                .setLastName(profile.getFamilyName()).build();
    }

    public void updateStatus(IndigoDC api, String message) {

    }
}
