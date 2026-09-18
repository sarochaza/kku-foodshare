document.addEventListener(
    "DOMContentLoaded",
    function () {

        const accountMenu =
            document.querySelector(
                "[data-account-menu]"
            );

        if (!accountMenu) {
            return;
        }

        const accountMenuButton =
            accountMenu.querySelector(
                ".account-menu-button"
            );

        const accountDropdown =
            accountMenu.querySelector(
                ".account-dropdown"
            );

        if (
            !accountMenuButton
            || !accountDropdown
        ) {
            return;
        }


        function setAccountMenuOpen(isOpen) {

            accountDropdown.hidden =
                !isOpen;

            accountMenuButton.setAttribute(
                "aria-expanded",
                String(isOpen)
            );

            accountMenu.classList.toggle(
                "is-open",
                isOpen
            );
        }


        accountMenuButton.addEventListener(
            "click",
            function (event) {

                event.stopPropagation();

                const isCurrentlyOpen =
                    accountMenuButton.getAttribute(
                        "aria-expanded"
                    ) === "true";

                setAccountMenuOpen(
                    !isCurrentlyOpen
                );
            }
        );


        document.addEventListener(
            "click",
            function (event) {

                if (
                    !accountMenu.contains(
                        event.target
                    )
                ) {
                    setAccountMenuOpen(false);
                }
            }
        );


        document.addEventListener(
            "keydown",
            function (event) {

                if (
                    event.key === "Escape"
                    && accountMenuButton.getAttribute(
                        "aria-expanded"
                    ) === "true"
                ) {
                    setAccountMenuOpen(false);

                    accountMenuButton.focus();
                }
            }
        );

    }
);