function loadStats() {
    let loader = document.getElementById("loading-window");

    setTimeout(() => {
        loader.classList.remove("is-loading");
    }, 1000);
}