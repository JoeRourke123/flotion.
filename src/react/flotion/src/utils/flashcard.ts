import {UnderstandingLevel} from "./index";

export type Flashcard = {
    id: string,
    understanding: UnderstandingLevel,
    modules: string[],
    question: string,
    answer: string,
}
