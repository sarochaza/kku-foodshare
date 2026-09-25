document.addEventListener(
    "DOMContentLoaded",
    function () {

        const mapElement =
            document.getElementById("foodMap");

        const statusElement =
            document.querySelector(
                "[data-map-status]"
            );

        const locateUserButton =
            document.querySelector(
                "[data-locate-user]"
            );

        if (!mapElement) {
            return;
        }

        if (typeof L === "undefined") {
            if (statusElement) {
                statusElement.textContent =
                    "ไม่สามารถโหลดแผนที่ได้";
            }

            return;
        }

        const KKU_CENTER = [
            16.4745,
            102.8237
        ];

        const map = L
            .map("foodMap")
            .setView(
                KKU_CENTER,
                15
            );

        let currentUserCoordinates = null;
        let userLocationMarker = null;
        let userAccuracyCircle = null;

        L.tileLayer(
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            {
                maxZoom: 19,
                attribution:
                    "&copy; OpenStreetMap contributors"
            }
        ).addTo(map);

        function formatDistance(
            distanceInMeters
        ) {
            if (distanceInMeters < 1000) {
                return (
                    Math.round(
                        distanceInMeters
                    ) + " เมตร"
                );
            }

            return (
                (
                    distanceInMeters / 1000
                ).toFixed(1) +
                " กิโลเมตร"
            );
        }

        function requestUserLocation() {

            if (!navigator.geolocation) {
                if (statusElement) {
                    statusElement.textContent =
                        "อุปกรณ์นี้ไม่รองรับการระบุตำแหน่ง";
                }

                return;
            }

            navigator.geolocation
                .getCurrentPosition(
                    function (position) {

                        const userLatitude =
                            position.coords.latitude;

                        const userLongitude =
                            position.coords.longitude;

                        const userAccuracy =
                            position.coords.accuracy;

                        currentUserCoordinates =
                            L.latLng(
                                userLatitude,
                                userLongitude
                            );

                        if (userLocationMarker) {
                            map.removeLayer(
                                userLocationMarker
                            );
                        }

                        if (userAccuracyCircle) {
                            map.removeLayer(
                                userAccuracyCircle
                            );
                        }

                        userAccuracyCircle =
                            L.circle(
                                currentUserCoordinates,
                                {
                                    radius:
                                        userAccuracy,
                                    color:
                                        "#60a5fa",
                                    weight: 1,
                                    fillColor:
                                        "#93c5fd",
                                    fillOpacity:
                                        0.18
                                }
                            ).addTo(map);

                        userLocationMarker =
                            L.circleMarker(
                                currentUserCoordinates,
                                {
                                    radius: 10,
                                    color:
                                        "#ffffff",
                                    weight: 3,
                                    fillColor:
                                        "#2563eb",
                                    fillOpacity: 1
                                }
                            )
                                .addTo(map)
                                .bindPopup(
                                    "📍 คุณอยู่ที่นี่"
                                )
                                .openPopup();

                        map.setView(
                            currentUserCoordinates,
                            17
                        );

                        if (statusElement) {
                            statusElement.textContent =
                                "พบตำแหน่งของคุณแล้ว";
                        }
                    },
                    function (error) {

                        let locationErrorMessage =
                            "ไม่สามารถระบุตำแหน่งของคุณได้";

                        if (
                            error.code ===
                            error.PERMISSION_DENIED
                        ) {
                            locationErrorMessage =
                                "กรุณาอนุญาตการเข้าถึงตำแหน่ง";
                        }

                        if (statusElement) {
                            statusElement.textContent =
                                locationErrorMessage;
                        }
                    }
                );
        }

        function createFoodPopup(foodPost) {

            const popup =
                document.createElement("div");

            popup.className =
                "food-map-popup";

            const title =
                document.createElement(
                    "strong"
                );

            title.textContent =
                foodPost.title ||
                "อาหารแบ่งปัน";

            const pickupLocation =
                document.createElement("p");

            pickupLocation.textContent =
                "📍 " +
                (
                    foodPost.pickupLocationName ||
                    "ไม่ระบุจุดรับ"
                );

            const quantity =
                document.createElement("p");

            quantity.textContent =
                "🍱 เหลือ " +
                (foodPost.quantity ?? 0) +
                " " +
                (
                    foodPost.unit ||
                    "รายการ"
                );

            const availableUntil =
                document.createElement("p");

            const availableUntilText =
                foodPost.availableUntil
                    ? new Date(
                        foodPost.availableUntil
                    ).toLocaleString(
                        "th-TH",
                        {
                            dateStyle: "short",
                            timeStyle: "short"
                        }
                    )
                    : "ไม่ระบุเวลา";

            availableUntil.textContent =
                "⏰ รับได้ถึง " +
                availableUntilText;

            popup.appendChild(title);
            popup.appendChild(
                pickupLocation
            );
            popup.appendChild(quantity);
            popup.appendChild(
                availableUntil
            );

            if (currentUserCoordinates) {

                const foodCoordinates =
                    L.latLng(
                        foodPost.latitude,
                        foodPost.longitude
                    );

                const distanceInMeters =
                    map.distance(
                        currentUserCoordinates,
                        foodCoordinates
                    );

                const distance =
                    document.createElement("p");

                distance.textContent =
                    "📏 ห่างจากคุณ " +
                    formatDistance(
                        distanceInMeters
                    );

                popup.appendChild(distance);
            }

            const directionsLink =
                document.createElement("a");

            directionsLink.className =
                "food-map-directions";

            directionsLink.textContent =
                "🧭 นำทางไปจุดรับ";

            directionsLink.href =
                "https://www.google.com/maps/dir/?api=1&destination=" +
                encodeURIComponent(
                    foodPost.latitude +
                    "," +
                    foodPost.longitude
                );

            directionsLink.target =
                "_blank";

            directionsLink.rel =
                "noopener noreferrer";

            popup.appendChild(
                directionsLink
            );

            return popup;
        }

        if (locateUserButton) {
            locateUserButton.addEventListener(
                "click",
                requestUserLocation
            );
        }

        requestUserLocation();

        fetch("/api/food-posts/map")
            .then(function (response) {

                if (!response.ok) {
                    throw new Error(
                        "Food post API request failed"
                    );
                }

                return response.json();
            })
            .then(function (foodPosts) {

                foodPosts.forEach(
                    function (foodPost) {

                        const marker =
                            L.marker([
                                foodPost.latitude,
                                foodPost.longitude
                            ]).addTo(map);

                        marker.bindPopup(
                            function () {
                                return createFoodPopup(
                                    foodPost
                                );
                            }
                        );
                    }
                );

                if (statusElement) {
                    statusElement.textContent =
                        foodPosts.length > 0
                            ? "พบอาหาร " +
                                foodPosts.length +
                                " จุด"
                            : "ยังไม่มีอาหารที่พร้อมรับ";
                }
            })
            .catch(function () {

                if (statusElement) {
                    statusElement.textContent =
                        "ไม่สามารถโหลดตำแหน่งอาหารได้";
                }
            });
    }
);