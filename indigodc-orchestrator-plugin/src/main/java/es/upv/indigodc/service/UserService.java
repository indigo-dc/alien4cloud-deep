package es.upv.indigodc.service;

import alien4cloud.security.AuthorizationUtil;
import alien4cloud.security.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * Wrap the A4C method of obtaining the currently logged in user This wrapper is needed to avoid
 * changes in multiple parts of the code when the API changes.
 *
 * @author asalic
 */
@Service
@Slf4j
public class UserService {

  /**
   * Obtain the the currently logged in user.
   *
   * @return the A4C user instance
   */
  public User getCurrentUser() {
    User user = AuthorizationUtil.getCurrentUser();
    // log.info("Username is: " + u.getUsername());
    // log.info("Password plain is: " + u.getPlainPassword());
    return user;
  }
}
