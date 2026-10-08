package com.kku.foodshare;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

class MailDeploymentConfigTest {
  private Properties compose(String name) {
    Path root = Path.of(System.getProperty("basedir")).getParent();
    var yaml = new YamlPropertiesFactoryBean();
    yaml.setResources(new FileSystemResource(root.resolve(name)));
    return yaml.getObject();
  }

  @Test
  void bothComposeEntrypointsPassTheEmailSettingsToTheApplication() {
    var primary = compose("compose.yaml");
    var course = compose("docker-compose.yml");
    assertEquals(primary, course, "Both supported Compose filenames must stay aligned");
    for (String name : new String[] {"MAIL_ENABLED", "MAIL_PROVIDER", "BREVO_API_KEY"}) {
      assertNotNull(primary.getProperty("services.app.environment." + name),
          name + " in .env must reach the app container");
    }
    assertEquals("${MAIL_ENABLED:-false}", primary.getProperty("services.app.environment.MAIL_ENABLED"));
  }

  @Test
  void localMailTestingUsesACapturedInboxAndThePublishedApplicationPort() {
    var local = compose("compose.mail-test.yaml");
    String prefix = "services.app.environment.";
    assertEquals("true", local.getProperty(prefix + "MAIL_ENABLED"));
    assertEquals("smtp", local.getProperty(prefix + "MAIL_PROVIDER"));
    assertEquals("mailpit", local.getProperty(prefix + "SPRING_MAIL_HOST"));
    assertEquals("1025", local.getProperty(prefix + "SPRING_MAIL_PORT"));
    assertEquals("false", local.getProperty(prefix + "SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH"));
    assertEquals("http://127.0.0.1:${APP_PORT:-8080}", local.getProperty(prefix + "APP_BASE_URL"));
    assertEquals("127.0.0.1:8025:8025", local.getProperty("services.mailpit.ports[0]"));
    assertTrue(local.stringPropertyNames().stream().noneMatch(name ->
        name.startsWith("services.db.") || name.startsWith("volumes.")),
        "Mail testing must not replace the database or its volume configuration");
  }
}
