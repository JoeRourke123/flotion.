/**
 *  Code for the homepage.
 */

let MODULES = [];

let HOME_STEP = 0;

async function populateModules() {
    let loader = document.getElementById("loading-window");
    let moduleList = document.getElementById("module-list");

    try {
        let moduleRequest = await fetch("/modules");
        let fetchedModules = await moduleRequest.json();

        if (fetchedModules.hasOwnProperty("modules")) {
            for (let moduleName of fetchedModules["modules"]) {
                moduleList.innerHTML += `
                    <a class="button primary outline module-button" onclick="selectModule(this)">${moduleName}</a>
                `;
            }

            MODULES = fetchedModules;
        }
    } catch (e) {
        console.log(e);
    }

    if (MODULES.length < 1) {
        moduleList.innerHTML = "<h2>No modules could be found</h2>";
    }

    setTimeout(() => {
        loader.classList.remove("is-loading");
    }, 1000);
}

function selectModule(e) {
    let moduleList = document.getElementById("module-list");
    let quizButton = document.getElementById("start-button");


    for (let child of moduleList.children) {
        if (child == e) {
            child.classList.toggle("outline");
        } else {
            child.classList.add("outline");
        }
    }

    if (!e.classList.contains("outline")) {
        localStorage.setItem("module", e.innerText);
        quizButton.innerText = "Next";
    } else {
        localStorage.removeItem("module");
        quizButton.innerText = "Skip";
    }
}

function selectDifficulty(e) {
    let moduleList = document.getElementById("module-list");

    for (let child of moduleList.children) {
        if (child == e) {
            e.classList.toggle("outline");
        } else {
            child.classList.add("outline");
        }
    }

    if (!e.classList.contains("outline")) {
        localStorage.setItem("difficulty", e.innerText);
    } else {
        localStorage.removeItem("difficulty");
    }
}

function beginQuiz() {
    let quizOptions = document.getElementById("quiz-options");
    let actionButton = document.getElementById("start-button");
    let moduleList = document.getElementById("module-list");

    if (actionButton.style.opacity !== "0") {
        if (HOME_STEP === 0) {
            quizOptions.style.opacity = "0";
            actionButton.style.opacity = "0";

            setTimeout(function () {
                moduleList.innerHTML = "";

                for (let level of ["Red", "Yellow", "Green"]) {
                    moduleList.innerHTML += `
                    <a class="button primary outline module-button" onclick="selectDifficulty(this)">${level}</a>
                `;
                }

                quizOptions.children[0].innerText = "Select Understanding";
                actionButton.innerText = "Start";
                quizOptions.style.opacity = "1";
                actionButton.style.opacity = "1";
            }, 550);

            HOME_STEP++;
        } else if (HOME_STEP === 1) {
            location.href = "/questions";
        }
    }
}

/**
 * Start of Quiz Page Code
 */

let item = {};

async function getQuestion() {
    let loader = document.getElementById("loading-window");
    loader.classList.add("is-loading");

    let card = document.getElementById("question-window");
    card.style.opacity = "1";
    card.classList.remove("is-hidden");

    let cardTitle = document.getElementById("question-title");
    let cardModule = document.getElementById("question-module");
    let cardDifficulty = document.getElementById("question-difficulty");

    let mod = localStorage.getItem("module");
    if (mod == null) {
        mod = "all";
    }
    let difficulty = localStorage.getItem("difficulty");
    if (difficulty == null) {
        difficulty = "";
    } else {
        difficulty = `__${difficulty}__`;
    }

    const resp = await fetch(`/q/${mod}${difficulty}`);
    if (resp.ok) {
        item = await resp.json();

        if (item.hasOwnProperty("error")) {
            card.innerHTML = "<h1>No cards exist for this module yet!</h1>";
        } else {
            cardTitle.innerText = item["title"];
            cardModule.innerText = item["module"];

            cardDifficulty.classList.remove("red", "green", "yellow");
            cardDifficulty.classList.add(item["level"]);
            buildAnswer(item['content']);
        }
    }

    loader.classList.remove("is-loading");
}

function buildAnswer(contents) {
    let content = "";

    for (let item of contents) {
        switch (item["type"]) {
            case "text":
                content += `<p>${item['value']}</p><br />`;
                break;
            case "img":
                content += `<img src="${item['value']}" class="image" /><br />`
                break;
            case "list":
                content += `<ul>`;
                for (let element of item['value']) {
                    content += `<li>${element}</li>`;
                }
                content += `</ul><br />`
        }
    }

    content.substring(0, content.length - 6);
    document.getElementById("answer").innerHTML = content;
}

async function answerQuestion(correct) {
    let loader = document.getElementById("loading-window");
    loader.classList.add("is-loading");

    if (item.hasOwnProperty("id")) {

        const doCorrect = fetch("/c/" + item["id"], {
            method: "POST"
        });
    }

    getQuestion();
}

function toggleOpacity() {
    let e = document.getElementById("question-window");

    if (e.style.opacity !== "0") {
        e.style.opacity = "0";
        setTimeout(() => {
            e.classList.add("is-hidden");
        }, 520);
    } else {
        e.classList.remove("is-hidden");
        setTimeout(() => {
            e.style.opacity = "1";
        }, 20);
    }
}