document.addEventListener(
    "DOMContentLoaded",
    function () {

        const mapElement =
                document.getElementById(
                    "foodMap"
                );

        const statusElement =
                document.querySelector(
                    "[data-map-status]"
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

        L.tileLayer(
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            {
                maxZoom: 19,
                attribution:
                    "&copy; OpenStreetMap contributors"
            }
        ).addTo(map);

        if (statusElement) {
            statusElement.textContent =
                    "แผนที่พร้อมใช้งาน";
        }

    }
);