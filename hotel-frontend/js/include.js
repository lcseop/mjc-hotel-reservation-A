async function loadComponent(id, file) {
    const target = document.getElementById(id);

    if (!target) {
        return;
    }

    const response = await fetch(file);
    const html = await response.text();
    target.innerHTML = html;
    document.dispatchEvent(new CustomEvent("component:loaded", { detail: { id } }));
}

loadComponent("header", "component/header.html");
loadComponent("searchHeader", "component/search-header.html");
loadComponent("footer", "component/footer.html");
