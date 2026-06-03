globalThis.addEventListener("load", function () {
    const a = document.querySelector("a.PostLogoutRedirectUri");
    if (a) {
        globalThis.location = a.href;
    }
});
