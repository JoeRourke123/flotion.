import {UnderstandingLevel} from "./index";

export type Flashcard = {
    id: string,
    understanding: UnderstandingLevel,
    modules: string[],
    question: string,
    answer: string,
}

export function toggleFlashcard(showQuestion: boolean, setShowQuestion: { (b: boolean): void }) {
    let question = document.getElementById("question");
    let answer = document.getElementById("answer");

    if(question != null && answer != null) {
        if(!showQuestion) {
            question.style.display = "initial";
            question.style.zIndex = "20";
        } else {
            answer.style.display = "initial";
        }

        setTimeout(() => {
            //@ts-ignore
            question.style.opacity = ( showQuestion ? "0" : "1" );
        }, 50);
        setTimeout(() => {
            setShowQuestion(!showQuestion);

            if(showQuestion) {
                //@ts-ignore
                question.style.display = "none";
                //@ts-ignore
                question.style.zIndex = "0";
            } else {
                //@ts-ignore
                answer.style.display = "none";
            }
        }, 500);
    }
}

export function toggleDrawingWindow(showDrawing: boolean, setShowDrawing: { (b: boolean): void }) {
    let drawing = document.getElementById("drawing");

    if(drawing != null) {
        if(showDrawing) {
            drawing.style.opacity = "0";

            setTimeout(() => {
                //@ts-ignore
                drawing.style.zIndex = "-100";
            }, 500);
        } else {
            drawing.style.zIndex = "100";

            setTimeout(() => {
                //@ts-ignore
                drawing.style.opacity = "1";
            }, 50);
        }

        setShowDrawing(!showDrawing);
    }
}
