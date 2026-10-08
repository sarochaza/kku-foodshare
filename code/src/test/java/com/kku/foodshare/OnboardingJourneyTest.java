package com.kku.foodshare;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import java.sql.DriverManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:onboarding-journey;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "app.pickup-reminder.scheduled=false"})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OnboardingJourneyTest {
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

  private User member() {
    var member = new User();
    member.setEmail(UUID.randomUUID() + "@guide.test");
    member.setDisplayName("Guide member");
    member.setPassword("unused");
    return users.saveAndFlush(member);
  }

  private void needsGuide(String email, String path, boolean expected) throws Exception {
    mvc.perform(get(path).with(user(email)))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(
            "<meta name=\"onboarding-needed\" content=\"" + expected + "\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"home-guide\"")));
  }

  @Test void registrationAndFirstLoginOpenTheGuideOnTheUnchangedLoginDestination() throws Exception {
    String email = UUID.randomUUID() + "@guide.test";
    mvc.perform(post("/register").with(csrf()).param("email", email)
            .param("displayName", "New guide member").param("password", "Password123!"))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    var login = mvc.perform(post("/login").with(csrf()).param("email", email)
            .param("password", "Password123!"))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home"))
        .andReturn();
    var session = (MockHttpSession) login.getRequest().getSession(false);
    assertNotNull(session);
    mvc.perform(get("/home").session(session))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(
            "<meta name=\"onboarding-needed\" content=\"true\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"home-guide\"")));
    needsGuide(email, "/", true);
    needsGuide(email, "/explore", true);
  }

  @Test void completionBelongsToOneAccountAndSurvivesAnotherLoginOrBrowser() throws Exception {
    User first = member(), second = member();
    mvc.perform(post("/api/v1/me/onboarding").with(user(first.getEmail())).with(csrf())
            .param("userId", second.getId().toString()))
        .andExpect(status().isNoContent());
    for (String path : new String[] {"/home", "/explore", "/"}) {
      needsGuide(first.getEmail(), path, false);
      needsGuide(second.getEmail(), path, true);
    }
    Long version = jdbc.queryForObject("select version from users where id = ?", Long.class, first.getId());
    mvc.perform(post("/api/v1/me/onboarding").with(user(first.getEmail())).with(csrf()))
        .andExpect(status().isNoContent());
    assertEquals(version, jdbc.queryForObject("select version from users where id = ?", Long.class, first.getId()));
  }

  @Test void openingOrRefreshingDoesNotMarkAnAccountAsFinished() throws Exception {
    User member = member();
    needsGuide(member.getEmail(), "/home", true);
    needsGuide(member.getEmail(), "/home", true);
    needsGuide(member.getEmail(), "/?guide=1", true);
  }

  @Test void guestsAndRequestsWithoutCsrfCannotChangeCompletion() throws Exception {
    User member = member();
    mvc.perform(post("/api/v1/me/onboarding").with(csrf())).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/v1/me/onboarding").with(user(member.getEmail())))
        .andExpect(status().isForbidden());
    needsGuide(member.getEmail(), "/home", true);
    mvc.perform(get("/explore")).andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(
            "<meta name=\"onboarding-needed\" content=\"false\"")));
  }

  @Test void migrationKeepsExistingAccountsAndDefaultsFutureAccountsToTheGuide() throws Exception {
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:guide-migration;MODE=PostgreSQL", "sa", "")) {
      var statement = connection.createStatement();
      statement.execute("create table users(id bigint primary key, display_name varchar(80))");
      statement.execute("insert into users values(1, 'Existing member')");
      ScriptUtils.executeSqlScript(connection,
          new ClassPathResource("db/migration/V10__account_onboarding.sql"));
      statement.execute("insert into users(id, display_name) values(2, 'New member')");
      try (var rows = statement.executeQuery("select id, display_name, onboarding_completed from users order by id")) {
        assertTrue(rows.next()); assertEquals("Existing member", rows.getString(2)); assertTrue(rows.getBoolean(3));
        assertTrue(rows.next()); assertEquals("New member", rows.getString(2)); assertFalse(rows.getBoolean(3));
        assertFalse(rows.next());
      }
    }
  }
}
