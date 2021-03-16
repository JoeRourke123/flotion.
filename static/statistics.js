async function loadStats() {
    let loader = document.getElementById("loading-window");

    let statsRequest = await fetch("/s");
    let stats = await statsRequest.json();

    let totalCorrect = document.getElementById("total_corrects");
    totalCorrect.innerText = stats["total_corrects"];

    let totalCards = stats["total_cards"];
    for(let level of ["red", "yellow", "green"]) {
        let element = document.getElementById("total_" + level);
        let totalLevel = stats["total_" + level];
        element.innerText = `${totalLevel}/${totalCards}`;
    }

    let moduleRanking = document.getElementById("ranked_modules");
    for(let mod of stats["ranked_modules"]) {
        moduleRanking.innerHTML += `${mod}<br />`;
    }

    setTimeout(() => {
        loader.classList.remove("is-loading");
    }, 1000);
}