package com.kku.foodshare;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStreamReader;
import java.sql.*;
import org.junit.jupiter.api.Test;

class OfflineMigrationTest {
  @Test
  void migrationPreservesLegacyRowsAndAddsTheCombinedStockConstraint() throws Exception {
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:offline-migration;MODE=PostgreSQL", "sa", "")) {
      connection.createStatement().execute("CREATE TABLE food_posts (id BIGINT PRIMARY KEY, quantity INTEGER NOT NULL, reserved_quantity INTEGER NOT NULL, collected_quantity INTEGER NOT NULL, CONSTRAINT post_quantity CHECK (quantity > 0 AND reserved_quantity >= 0 AND collected_quantity >= 0 AND reserved_quantity + collected_quantity <= quantity))");
      connection.createStatement().execute("INSERT INTO food_posts VALUES (42, 5, 3, 1)");
      try (var script = new InputStreamReader(getClass().getResourceAsStream("/db/migration/V3__offline_distribution.sql"), java.nio.charset.StandardCharsets.UTF_8)) {
        org.h2.tools.RunScript.execute(connection, script);
      }
      var row = connection.createStatement().executeQuery("SELECT * FROM food_posts WHERE id = 42");
      assertTrue(row.next()); assertEquals(5, row.getInt("quantity"));
      assertEquals(3, row.getInt("reserved_quantity")); assertEquals(1, row.getInt("collected_quantity"));
      assertEquals(0, row.getInt("offline_quantity"));
      connection.createStatement().execute("UPDATE food_posts SET offline_quantity = 1 WHERE id = 42");
      assertThrows(SQLException.class, () -> connection.createStatement().execute("UPDATE food_posts SET offline_quantity = 2 WHERE id = 42"));
      assertThrows(SQLException.class, () -> connection.createStatement().execute("UPDATE food_posts SET offline_quantity = -1 WHERE id = 42"));
    }
  }
}
