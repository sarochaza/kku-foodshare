package com.kku.foodshare.controller.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.kku.foodshare.dto.response.UserProfileResponse;
import com.kku.foodshare.service.UserProfileService;

class DashboardControllerTest {

    // ทดสอบว่าเปิดหน้า Dashboard ถูกต้อง
    @Test
void dashboardShouldAddEmailAccountProfileToModel() {

    UserProfileService userProfileService =
            mock(UserProfileService.class);

    UserProfileResponse profile =
            new UserProfileResponse(
                    "ชมพู่ เสาทอง",
                    "sarocha@kku.ac.th",
                    "บัญชีอีเมล"
            );

    when(
            userProfileService.getProfile(
                    "sarocha@kku.ac.th",
                    "บัญชีอีเมล"
            )
    ).thenReturn(profile);

    DashboardController controller =
            new DashboardController(
                    userProfileService
            );

    Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                    "sarocha@kku.ac.th",
                    "password"
            );

    Model model =
            new ExtendedModelMap();

    String viewName =
            controller.dashboard(
                    authentication,
                    model
            );

    assertEquals(
            "dashboard",
            viewName
    );

    assertSame(
            profile,
            model.getAttribute("currentUser")
    );

    verify(userProfileService)
            .getProfile(
                    "sarocha@kku.ac.th",
                    "บัญชีอีเมล"
            );
}

    // ทดสอบว่า Dashboard มีรายการอาหารใกล้คุณ
    @Test
    void dashboardShouldContainNearbyFoodSection() throws Exception {

        String html = Files.readString(
                Path.of("src/main/resources/templates/dashboard.html")
        );

        assertTrue(
                html.contains("รายการอาหารใกล้คุณ")
        );
    }
    // ทดสอบว่า Dashboard มี Dropdown บัญชีของผู้ใช้
@Test
void dashboardShouldContainAccountDropdown()
        throws Exception {

    String html = Files.readString(
            Path.of(
                    "src/main/resources/templates/dashboard.html"
            )
    );

    assertTrue(
            html.contains("account-menu-button")
    );

    assertTrue(
            html.contains("account-dropdown")
    );

    assertTrue(
            html.contains("aria-expanded=\"false\"")
    );

    assertTrue(
            html.contains("th:action=\"@{/logout}\"")
    );
}
// ทดสอบว่า Dashboard โหลด JavaScript
// และรองรับการเปิดปิด Account Dropdown
@Test
void dashboardShouldLoadAccountDropdownScript()
        throws Exception {

    String html = Files.readString(
            Path.of(
                    "src/main/resources/templates/dashboard.html"
            )
    );

    Path scriptPath = Path.of(
            "src/main/resources/static/js/dashboard.js"
    );

    assertTrue(
            html.contains(
                    "th:src=\"@{/js/dashboard.js}\""
            )
    );

    assertTrue(
            Files.exists(scriptPath)
    );

    String script = Files.readString(scriptPath);

    assertTrue(
            script.contains("setAccountMenuOpen")
    );

    assertTrue(
            script.contains("aria-expanded")
    );

    assertTrue(
            script.contains("Escape")
    );

    assertTrue(
            script.contains("accountMenu.contains")
    );
}
@Test
void dashboardShouldAddGoogleAccountProfileToModel() {

    UserProfileService userProfileService =
            mock(UserProfileService.class);

    UserProfileResponse profile =
            new UserProfileResponse(
                    "Sarocha Saothong",
                    "sarocha@gmail.com",
                    "บัญชี Google"
            );

    when(
            userProfileService.getProfile(
                    "sarocha@gmail.com",
                    "บัญชี Google"
            )
    ).thenReturn(profile);

    OAuth2User oauth2User =
            new DefaultOAuth2User(
                    List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_USER"
                            )
                    ),
                    Map.of(
                            "sub",
                            "10987654321",
                            "email",
                            "sarocha@gmail.com",
                            "name",
                            "Sarocha Saothong"
                    ),
                    "sub"
            );

    Authentication authentication =
            new OAuth2AuthenticationToken(
                    oauth2User,
                    oauth2User.getAuthorities(),
                    "google"
            );

    Model model =
            new ExtendedModelMap();

    DashboardController controller =
            new DashboardController(
                    userProfileService
            );

    String viewName =
            controller.dashboard(
                    authentication,
                    model
            );

    assertEquals(
            "dashboard",
            viewName
    );

    assertSame(
            profile,
            model.getAttribute("currentUser")
    );

    verify(userProfileService)
            .getProfile(
                    "sarocha@gmail.com",
                    "บัญชี Google"
            );
}
// ทดสอบว่า Dashboard ใช้ Leaflet ภายในโปรเจกต์และมีพื้นที่แสดงแผนที่
@Test
void dashboardShouldContainLeafletFoodMap()
        throws Exception {

    String html = Files.readString(
            Path.of(
                    "src/main/resources/templates/dashboard.html"
            )
    );

    Path mapScriptPath = Path.of(
            "src/main/resources/static/js/dashboard-map.js"
    );

    Path leafletCssPath = Path.of(
            "src/main/resources/static/vendor/leaflet/leaflet.css"
    );

    Path leafletScriptPath = Path.of(
            "src/main/resources/static/vendor/leaflet/leaflet.js"
    );

    assertTrue(
            html.contains("id=\"foodMap\"")
    );

    assertTrue(
            html.contains("data-locate-user")
    );

    assertTrue(
            html.contains(
                    "th:href=\"@{/vendor/leaflet/leaflet.css}\""
            )
    );

    assertTrue(
            html.contains(
                    "th:src=\"@{/vendor/leaflet/leaflet.js}\""
            )
    );

    assertTrue(
            html.contains(
                    "th:src=\"@{/js/dashboard-map.js}\""
            )
    );

    assertTrue(
            Files.exists(leafletCssPath)
    );

    assertTrue(
            Files.exists(leafletScriptPath)
    );

    assertTrue(
            Files.exists(mapScriptPath)
    );
}
// ทดสอบว่าสคริปต์แผนที่โหลดโพสต์อาหารจาก Map API
@Test
void dashboardMapShouldLoadFoodPostsFromApi()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains(
                    "fetch(\"/api/food-posts/map\")"
            )
    );
}
// ทดสอบว่าสคริปต์สร้างหมุดอาหารจากพิกัดของแต่ละโพสต์
@Test
void dashboardMapShouldCreateMarkerForEachFoodPost()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains("foodPosts.forEach")
    );

    assertTrue(
            script.contains("foodPost.latitude")
    );

    assertTrue(
            script.contains("foodPost.longitude")
    );

    assertTrue(
            script.contains("L.marker")
    );
}
// ทดสอบว่าหมุดอาหารแสดงรายละเอียดโพสต์ใน Popup อย่างปลอดภัย
@Test
void dashboardMapMarkerShouldShowFoodPostPopup()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains("document.createElement")
    );

    assertTrue(
            script.contains("textContent")
    );

    assertTrue(
            script.contains("foodPost.title")
    );

    assertTrue(
            script.contains("foodPost.pickupLocationName")
    );

    assertTrue(
            script.contains("bindPopup")
    );
}
// ทดสอบว่า Popup แสดงจำนวนและเวลาสิ้นสุดของโพสต์อาหาร
@Test
void dashboardMapPopupShouldShowQuantityAndAvailableUntil()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains("foodPost.quantity")
    );

    assertTrue(
            script.contains("foodPost.unit")
    );

    assertTrue(
            script.contains("foodPost.availableUntil")
    );

    assertTrue(
            script.contains("toLocaleString")
    );
}
// ทดสอบว่า Popup มีปุ่มเปิดเส้นทางไปยังจุดรับอาหาร
@Test
void dashboardMapPopupShouldProvidePickupDirections()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains(
                    "https://www.google.com/maps/dir/?api=1&destination="
            )
    );

    assertTrue(
            script.contains("นำทางไปจุดรับ")
    );

    assertTrue(
            script.contains("target")
    );

    assertTrue(
            script.contains("_blank")
    );
}
// ทดสอบว่าแผนที่แจ้งเตือนเมื่อโหลดโพสต์อาหารไม่สำเร็จ
@Test
void dashboardMapShouldShowErrorWhenFoodPostsCannotLoad()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains("response.ok")
    );

    assertTrue(
            script.contains(".catch")
    );

    assertTrue(
            script.contains(
                    "ไม่สามารถโหลดตำแหน่งอาหารได้"
            )
    );
}
// ทดสอบว่าปุ่มตำแหน่งของฉันเรียกใช้ Geolocation ของเบราว์เซอร์
@Test
void dashboardMapShouldRequestCurrentUserLocation()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains(
                    "[data-locate-user]"
            )
    );

    assertTrue(
            script.contains(
                    "navigator.geolocation"
            )
    );

    assertTrue(
            script.contains(
                    "getCurrentPosition"
            )
    );

    assertTrue(
            script.contains(
                    "addEventListener"
            )
    );
}
// ทดสอบว่าแผนที่แสดงหมุดและเลื่อนไปยังตำแหน่งของผู้ใช้
@Test
void dashboardMapShouldShowCurrentUserLocation()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains(
                    "position.coords.latitude"
            )
    );

    assertTrue(
            script.contains(
                    "position.coords.longitude"
            )
    );

    assertTrue(
            script.contains(
                    "userLocationMarker"
            )
    );

    assertTrue(
            script.contains(
                    "L.circleMarker"
            )
    );

    assertTrue(
            script.contains(
                    "คุณอยู่ที่นี่"
            )
    );

    assertTrue(
            script.contains(
                    "map.setView"
            )
    );
}
// ทดสอบว่าแผนที่แจ้งข้อความเมื่อไม่สามารถเข้าถึงตำแหน่งผู้ใช้
@Test
void dashboardMapShouldHandleUserLocationError()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains(
                    "PERMISSION_DENIED"
            )
    );

    assertTrue(
            script.contains(
                    "กรุณาอนุญาตการเข้าถึงตำแหน่ง"
            )
    );

    assertTrue(
            script.contains(
                    "ไม่สามารถระบุตำแหน่งของคุณได้"
            )
    );
}
// ทดสอบว่าแผนที่แสดงขอบเขตความคลาดเคลื่อนของตำแหน่งผู้ใช้
@Test
void dashboardMapShouldShowUserLocationAccuracy()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains(
                    "position.coords.accuracy"
            )
    );

    assertTrue(
            script.contains(
                    "userAccuracyCircle"
            )
    );

    assertTrue(
            script.contains(
                    "L.circle("
            )
    );
}
// ทดสอบว่า Dashboard ขอตำแหน่งผู้ใช้เมื่อเปิดแผนที่
@Test
void dashboardMapShouldRequestLocationOnLoad()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains(
                    "requestUserLocation"
            )
    );

    assertTrue(
            script.contains(
                    "requestUserLocation();"
            )
    );
}

// ทดสอบว่า Popup แสดงระยะห่างระหว่างผู้ใช้กับจุดแบ่งปัน
@Test
void dashboardMapPopupShouldShowDistanceFromUser()
        throws Exception {

    String script = Files.readString(
            Path.of(
                    "src/main/resources/static/js/dashboard-map.js"
            )
    );

    assertTrue(
            script.contains(
                    "currentUserCoordinates"
            )
    );

    assertTrue(
            script.contains(
                    "map.distance"
            )
    );

    assertTrue(
            script.contains(
                    "formatDistance"
            )
    );

    assertTrue(
            script.contains(
                    "ห่างจากคุณ"
            )
    );
}
}