async function loadComponent(id, file) {
    const target = document.getElementById(id);

    if (!target) {
        return;
    }

    try {
        const response = await fetch(file);

        if (!response.ok) {
            throw new Error(file + " load failed");
        }

        const html = await response.text();
        target.innerHTML = html;
        document.dispatchEvent(new CustomEvent("component:loaded", { detail: { id } }));
    } catch (error) {
        target.innerHTML = "";
        document.dispatchEvent(new CustomEvent("component:error", { detail: { id, file } }));
    }
}

loadComponent("header", "component/header.html");
loadComponent("searchHeader", "component/search-header.html");
loadComponent("footer", "component/footer.html");
