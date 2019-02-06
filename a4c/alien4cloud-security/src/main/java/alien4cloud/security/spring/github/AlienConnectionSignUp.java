package alien4cloud.security.spring.github;

import alien4cloud.security.model.User;
import alien4cloud.security.users.IAlienUserDao;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UserProfile;

public class AlienConnectionSignUp implements ConnectionSignUp {

    private IAlienUserDao alienUserDao;

    private String[] roleList;

    public AlienConnectionSignUp(IAlienUserDao alienUserDao, String[] roleList) {
        this.alienUserDao = alienUserDao;
        this.roleList = roleList;
    }

    @Override
    public String execute(Connection<?> connection) {
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
        }
        return userId;
    }
}
