package alien4cloud.security.spring.github;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.github.connect.GitHubConnectionFactory;
import org.springframework.social.oidc.indigodc.connect.IndigoDCConnectionFactory;
import org.springframework.social.security.AuthenticationNameUserIdSource;

import alien4cloud.security.users.IAlienUserDao;

@Configuration
@EnableSocial
@Profile({"github-auth", "indigo-dc"})
public class SocialConfig implements SocialConfigurer {
    @Resource
    private IAlienUserDao alienUserDao;

    @Override
    public UserIdSource getUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer, Environment environment) {
        if (environment.acceptsProfiles("github-auth")) {
            connectionFactoryConfigurer.addConnectionFactory(new GitHubConnectionFactory("6dee0f1f3504e97c38cc", "c73348b4b6390d9c8bc63a88846fc593b5380b73"));
        }

        if (environment.acceptsProfiles("indigo-dc")) {
            connectionFactoryConfigurer.addConnectionFactory(
                    new IndigoDCConnectionFactory(
                            environment.getProperty("indigo-dc.iam.issuer"),
                            environment.getProperty("indigo-dc.iam.client-id"),
                            environment.getProperty("indigo-dc.iam.client-secret")));
        }
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return new AlienUserConnectionRepository(alienUserDao, connectionFactoryLocator);
    }
}
