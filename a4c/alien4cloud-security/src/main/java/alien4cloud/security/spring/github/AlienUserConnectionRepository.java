package alien4cloud.security.spring.github;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import alien4cloud.exception.NotFoundException;
import alien4cloud.security.model.Role;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.util.MultiValueMap;

import alien4cloud.security.model.User;
import alien4cloud.security.users.IAlienUserDao;

import javax.annotation.PostConstruct;

@Slf4j
@Profile("github-auth")
public class AlienUserConnectionRepository implements UsersConnectionRepository {
    private IAlienUserDao alienUserDao;

    @Value("${indigo-dc.roles}")
    private String roles;

    private String[] roleList;

    @Autowired
    public AlienUserConnectionRepository(IAlienUserDao alienUserDao) {
        this.alienUserDao = alienUserDao;
    }

    @PostConstruct
    public void init() {
        if (roles == null) {
            roleList = new String[]{};
        } else {
            String[] roleSplit = roles.split(",");
            List<String> goodRoles = Arrays.asList(roleSplit).stream()
                    .map(role -> {
                        try {
                            return Role.getStringFormatedRole(role);
                        } catch (NotFoundException e) {
                            return null;
                        }
                    })
                    .filter(role -> role != null)
                    .collect(Collectors.toList());
            roleList = goodRoles.toArray(new String[goodRoles.size()]);
        }
    }

    @Override
    public List<String> findUserIdsWithConnection(Connection<?> connection) {

        ConnectionKey key = connection.getKey();
        UserProfile profile = connection.fetchUserProfile();
        String userId = profile.getUsername() + "@" + key.getProviderId() + "::" + key.getProviderUserId();
        User user = alienUserDao.find(userId);
        if (user == null) {
            user = new User();
            user.setUsername(userId);
            user.setFirstName(profile.getFirstName());
            user.setLastName(profile.getLastName());
            user.setEmail(profile.getEmail());
            user.setRoles(roleList);
            alienUserDao.save(user);
            return Lists.newArrayList(userId);
            // TODO what connexion(s) means in spring sec ?
            // createConnectionRepository(newUserId).addConnection(connection);
        }
        return Lists.newArrayList(userId);
    }

    @Override
    public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
        log.info("Called findUserIdsConnectedTo with parameters ", providerId, providerUserIds);
        return Sets.newHashSet();
    }

    @Override
    public ConnectionRepository createConnectionRepository(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return new ConnectionRepository() {
            @Override
            public void updateConnection(Connection<?> connection) {
                log.info("Update a connection", connection);
            }

            @Override
            public void removeConnections(String providerId) {
                log.info("Remove all provider connection's", providerId);
            }

            @Override
            public void removeConnection(ConnectionKey connectionKey) {
                log.info("Remove connection by key ", connectionKey);
            }

            @Override
            public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
                log.info("Request primary connection by api ", apiType);
                return null;
            }

            @Override
            public <A> Connection<A> getConnection(Class<A> apiType, String providerUserId) {
                log.info("Request connection by api and user ", apiType, providerUserId);
                return null;
            }

            @Override
            public Connection<?> getConnection(ConnectionKey connectionKey) {
                log.info("Request connection by key ", connectionKey);
                return null;
            }

            @Override
            public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
                log.info("Request all connections for api", apiType);
                return null;
            }

            @Override
            public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUserIds) {
                log.info("Request all connections for users", providerUserIds);
                return null;
            }

            @Override
            public <A> List<Connection<A>> findConnections(Class<A> apiType) {
                log.info("Request all connections api", apiType);
                return null;
            }

            @Override
            public List<Connection<?>> findConnections(String providerId) {
                log.info("Request all connections for provider", providerId);
                return null;
            }

            @Override
            public MultiValueMap<String, Connection<?>> findAllConnections() {
                log.info("Request all connections");
                return null;
            }

            @Override
            public void addConnection(Connection<?> connection) {
                log.info("Add connection ", connection);
            }
        };
    }
}