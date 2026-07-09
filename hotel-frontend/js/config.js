(function (window) {
    const API_BASE = "http://localhost:33000/api";

    window.StayNowConfig = {
        apiBase: API_BASE,
        assetBase: API_BASE.replace(/\/api$/, ""),
        apiUrl: function (path) {
            return API_BASE + "/" + String(path || "").replace(/^\/+/, "");
        },
        assetUrl: function (path) {
            return this.assetBase + "/" + String(path || "").replace(/^\/+/, "");
        }
    };
})(window);
