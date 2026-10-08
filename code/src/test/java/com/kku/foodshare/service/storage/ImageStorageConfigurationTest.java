package com.kku.foodshare.service.storage;

import static org.junit.jupiter.api.Assertions.*;
import com.kku.foodshare.config.ImageStorageConfiguration;
import com.kku.foodshare.config.TimeConfig;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.MapPropertySource;

class ImageStorageConfigurationTest {
  @TempDir Path directory;
  @Test void unknownProviderCannotSilentlyUseTemporaryStorage() {
    for (String provider : new String[] {"cloudnary", "", "cloudinary "}) {
      new ApplicationContextRunner()
          .withUserConfiguration(LocalImageStorage.class, CloudinaryImageStorage.class,
              ImageStorageConfiguration.class, TimeConfig.class)
          .withPropertyValues("app.upload-dir=" + directory)
          .withInitializer(app -> app.getEnvironment().getPropertySources().addFirst(
              new MapPropertySource("untrimmed-provider", Map.of("app.images.provider", provider))))
          .run(app -> assertNotNull(app.getStartupFailure(), "Reject provider typos instead of falling back to local uploads"));
    }
  }

  @Test void localIsStillTheDefaultAndCloudinaryIsOnlySelectedWhenExplicitlyConfigured() {
    var context = new ApplicationContextRunner()
        .withUserConfiguration(LocalImageStorage.class, CloudinaryImageStorage.class,
            ImageStorageConfiguration.class, TimeConfig.class)
        .withPropertyValues("app.upload-dir=" + directory);
    context.run(app -> {
      assertNull(app.getStartupFailure());
      assertInstanceOf(LocalImageStorage.class, app.getBean(ImageStorage.class));
    });
    context.withPropertyValues("app.images.provider=cloudinary", "app.images.cloudinary.cloud-name=demo-cloud",
        "app.images.cloudinary.api-key=test-key", "app.images.cloudinary.api-secret=test-secret").run(app -> {
      assertNull(app.getStartupFailure());
      var selected = app.getBean(ImageStorage.class);
      assertInstanceOf(CloudinaryImageStorage.class, selected);
      String file = "cld-11111111-2222-3333-4444-555555555555.jpg";
      assertEquals("https://res.cloudinary.com/demo-cloud/image/upload/kku-foodshare/" + file, selected.publicUrl(file).toString());
      assertNotNull(app.getBean(LocalImageStorage.class), "Local storage still serves legacy images");
    });
  }
}
