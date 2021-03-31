async function loadStats() {
    let loader = document.getElementById("loading-window");

    let statsRequest = await fetch("/s");
    let stats = await statsRequest.json();

    let totalCards = stats["total_cards"];
    for(let level of ["red", "yellow", "green"]) {
        let element = document.getElementById("total_" + level);
        let totalLevel = stats["total_" + level];
        element.innerText = `${totalLevel}/${totalCards}`;
    }

    let moduleRanking = document.getElementById("modules");
    let modIndex = 0;
    for(let mod of stats["modules"]) {
        let total = stats["module_totals"][modIndex];
        let u = stats["module_understandings"][modIndex];
        moduleRanking.innerHTML += `${mod} <span>(
            ${u[0]}/${total}, 
            ${u[1]}/${total}, 
            ${u[2]}/${total}
        )</span><br />`;
        modIndex++;
    }

    setTimeout(() => {
        loader.classList.remove("is-loading");
    }, 1000);
}