package ca.gc.aafc.objectstore.api;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;

@Configuration
public class DinaAuthenticatedUserConfig {

  public static final String USER_NAME = "test_user";
  public static final Set<String> GROUPS = ImmutableSet.of(TestConfiguration.TEST_BUCKET, "Group 2");

  @Bean
  public DinaAuthenticatedUser dinaAuthenticatedUser() {
    return DinaAuthenticatedUser.builder().username(USER_NAME).groups(GROUPS).build();
  }
}
