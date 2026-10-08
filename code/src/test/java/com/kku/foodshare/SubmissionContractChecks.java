package com.kku.foodshare;

import com.kku.foodshare.config.ImageStorageConfiguration;
import com.kku.foodshare.config.TimeConfig;
import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.storage.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

/** Runs without a database or external provider; the JUnit wrapper invokes the same checks. */
public final class SubmissionContractChecks {
  private static final String ROOT = "com.kku.foodshare.";
  private SubmissionContractChecks() {}

  public static void layering() throws Exception {
    for (String name : List.of("MemberProfileController", "OwnerStockController", "PickupScanController")) {
      for (Field field : Class.forName(ROOT + "controller.api." + name).getDeclaredFields()) {
        Class<?> type = field.getType();
        if (type.getPackageName().startsWith(ROOT + "repository"))
          throw new AssertionError(name + " must call a service instead of a repository");
        if (type.getPackageName().startsWith(ROOT + "service") && !type.isInterface())
          throw new AssertionError(name + " must depend on a service interface");
      }
    }
  }

  public static void abstractions() throws Exception {
    for (String name : List.of("service.impl.FoodCatalogServiceImpl", "service.impl.ReservationServiceImpl",
        "service.impl.SavedPostServiceImpl", "service.impl.AccountServiceImpl", "service.impl.FoodPostServiceImpl",
        "service.impl.UserProfileServiceImpl", "service.impl.NotificationServiceImpl", "service.impl.PickupReminderJob",
        "service.storage.CloudinaryImageStorage")) {
      for (Field field : Class.forName(ROOT + name).getDeclaredFields()) {
        Class<?> type = field.getType();
        if ((type.getPackageName().startsWith(ROOT + "service")
            || type.getPackageName().startsWith(ROOT + "mapper")) && !type.isInterface())
          throw new AssertionError(name + " depends on concrete collaborator " + type.getSimpleName());
      }
    }
    for (Field field : Class.forName(ROOT + "mapper.PostViewMapper").getDeclaredFields()) {
      if (field.getType().getPackageName().startsWith(ROOT + "repository"))
        throw new AssertionError("DTO mapper must not perform repository access");
    }
  }

  public static void profile() throws Exception {
    User active = new User(); active.setId(42L); active.setDisplayName("สมาชิกทดสอบ"); active.setActive(true);
    User inactive = new User(); inactive.setId(43L); inactive.setActive(false);
    UserRepository users = (UserRepository) Proxy.newProxyInstance(UserRepository.class.getClassLoader(),
        new Class<?>[] {UserRepository.class}, (proxy, method, args) -> {
          if (method.getName().equals("findById")) {
            long id = (Long) args[0];
            return Optional.ofNullable(id == 42 ? active : id == 43 ? inactive : null);
          }
          throw new AssertionError("Unexpected repository operation: " + method.getName());
        });
    Object service = Class.forName(ROOT + "service.impl.MemberProfileServiceImpl")
        .getConstructor(UserRepository.class).newInstance(users);
    Method get = service.getClass().getMethod("get", long.class);
    Object response = get.invoke(service, 42L);
    require(response.getClass().getMethod("id").invoke(response).equals(42L), "Preserve member id");
    require(response.getClass().getMethod("name").invoke(response).equals("สมาชิกทดสอบ"), "Preserve display name");
    var json = new tools.jackson.databind.ObjectMapper().writeValueAsString(response);
    require(new tools.jackson.databind.ObjectMapper().readTree(json).size() == 2, "Keep the two-field API contract");
    for (long id : new long[] {43, 999}) {
      try { get.invoke(service, id); throw new AssertionError("Inactive/missing member must be 404"); }
      catch (InvocationTargetException error) {
        require(error.getCause() instanceof Problem p && p.status == 404, "Preserve inactive/missing 404");
      }
    }
  }

  public static void storage() throws Exception {
    Path directory = Files.createTempDirectory("foodshare-contract-");
    try {
      for (String provider : List.of("local", "cloudinary")) {
        try (var context = new AnnotationConfigApplicationContext()) {
          context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("contract", Map.of(
              "app.images.provider", provider, "app.upload-dir", directory.toString(),
              "app.images.cloudinary.cloud-name", "demo-cloud", "app.images.cloudinary.api-key", "test-key",
              "app.images.cloudinary.api-secret", "test-secret")));
          context.register(LocalImageStorage.class, CloudinaryImageStorage.class,
              ImageStorageConfiguration.class, TimeConfig.class);
          context.refresh();
          ImageStorage selected = context.getBean(ImageStorage.class);
          require(provider.equals("local") ? selected instanceof LocalImageStorage
              : selected instanceof CloudinaryImageStorage, "Select the configured image provider");
          String name = "11111111-2222-3333-4444-555555555555.jpg";
          Files.write(directory.resolve(name), new byte[] {1, 2, 3});
          require(Arrays.equals(selected.load(name).getInputStream().readAllBytes(), new byte[] {1, 2, 3}),
              "Legacy local image must remain readable with either provider");
          selected.remove(name);
          require(!Files.exists(directory.resolve(name)), "Legacy local deletion remains available");
        }
      }
    } finally { Files.deleteIfExists(directory); }
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  public static void main(String[] args) throws Exception {
    int failures = 0;
    for (String name : List.of("layering", "abstractions", "profile", "storage")) {
      try { SubmissionContractChecks.class.getMethod(name).invoke(null); System.out.println("PASS " + name); }
      catch (InvocationTargetException error) {
        failures++; System.out.println("FAIL " + name + ": " + error.getCause());
      }
    }
    if (failures != 0) throw new AssertionError(failures + " contract checks failed");
  }
}
