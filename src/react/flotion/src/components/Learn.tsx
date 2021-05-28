import React, {FC, useEffect, useState} from "react";
import {useAppSelector} from "../utils/hooks";
import {useHistory} from "react-router";
import {NetworkStatus, useLazyQuery, useMutation, useQuery} from "@apollo/client";
import {CORRECT_MUTATION, RANDOM_CARD_QUERY} from "../utils/gql";
import {Flashcard} from "../utils/flashcard";
import NoCards from "./NoCards";
import {EuiButton, EuiFlexGroup, EuiFlexItem, EuiLoadingSpinner, EuiText} from "@elastic/eui";
import {getHeaders} from "../utils/auth";
import {getUnderstanding, UnderstandingLevel} from "../utils";
import "../App.css";

const Learn: FC = () => {
    const token = useAppSelector((state) => state.userData.token);
    const parameters = useAppSelector((state) => state.parameters);

    const [card, setCard] = useState<Flashcard>();
    const [showQuestion, setShowQuestion] = useState(true);

    const {data, error, loading, refetch, networkStatus} = useQuery(RANDOM_CARD_QUERY, {
        ...getHeaders(token),
        variables: parameters,
        notifyOnNetworkStatusChange: true
    });
    const [markAsCorrect] = useMutation(CORRECT_MUTATION);

    const history = useHistory();

    function getColor(understanding: UnderstandingLevel): "danger" | "warning" | "secondary" {
        // @ts-ignore
        return {
            [UnderstandingLevel.RED]: "danger",
            [UnderstandingLevel.YELLOW]: "warning",
            [UnderstandingLevel.GREEN]: "secondary"
        }[understanding] || "danger";
    }

    function toggleQuestion() {
        let question = document.getElementById("question");
        let answer = document.getElementById("answer");

        if(question != null && answer != null) {
            if(!showQuestion) {
                question.style.display = "initial";
                question.style.zIndex = "20";
                answer.style.overflow = "hidden";
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
                    //@ts-ignore
                    answer.style.overflowY = "auto";
                }
            }, 500);
        }
    }

    function isLoading() {
        return loading || networkStatus === NetworkStatus.refetch;
    }

    async function newCard() {
        if(!showQuestion) {
            toggleQuestion();
        }
        await refetch();
    }

    async function gotCorrect() {
        newCard();
        markAsCorrect({
            variables: { card: card?.id },
            ...getHeaders(token)
        });
    }

    useEffect(() => {
        if (data !== undefined) {
            if (data.randomCard.response >= 400) {
                history.push("/error", data.randomCard);
            } else if (data.randomCard.card != null) {
                let cardData = {...data.randomCard.card};
                cardData.understanding = getUnderstanding(cardData.understanding);

                const flashcard: Flashcard = cardData;
                setCard(flashcard);
            }
        }
    }, [data]);

    if (isLoading()) return <div className="centeredContainer"><EuiLoadingSpinner size="xl"/></div>;
    if (data != null && data.response == 204) {
        return <NoCards/>
    } else if (card != null) {
        // @ts-ignore
        return (
            <div style={{height: "100vh", width: "100%"}}>
                <div id="question" className="eui-fullHeight question" onClick={() => toggleQuestion() }>
                    <EuiFlexGroup responsive={false} className="eui-fullHeight" direction="column" justifyContent="center"
                                  alignItems="center">
                        <EuiFlexItem/>
                        <EuiFlexItem grow={false}>
                            <EuiFlexGroup responsive={false} direction="column" justifyContent="center" alignItems="center">
                                <EuiFlexItem grow={false}>
                                    <EuiText>
                                        <h1>{card.question}</h1>
                                    </EuiText>
                                </EuiFlexItem>
                                <EuiFlexItem>
                                    <EuiFlexGroup gutterSize="s">
                                        <EuiFlexItem grow={false}>
                                            <EuiButton fill size="s" color={getColor(card.understanding)}>
                                            </EuiButton>
                                        </EuiFlexItem>
                                        {
                                            card.modules.map((e) => <EuiFlexItem grow={false}>
                                                <EuiButton size="s">
                                                    {e}
                                                </EuiButton>
                                            </EuiFlexItem>)
                                        }
                                    </EuiFlexGroup>
                                </EuiFlexItem>
                            </EuiFlexGroup>
                        </EuiFlexItem>
                        <EuiFlexItem/>
                        <EuiFlexItem grow={false}>
                            <span style={{color: "#AAAAAA"}}>click anywhere to show answer.</span>
                        </EuiFlexItem>
                    </EuiFlexGroup>
                </div>
                <div id="answer" className="eui-fullHeight answer" onClick={() => toggleQuestion()}>
                    <EuiFlexGroup responsive={false} className="eui-fullHeight" direction="column" justifyContent="center"
                                  alignItems="center">
                        <EuiFlexItem />
                        <EuiFlexItem grow={false}>
                            <EuiText>
                                <div dangerouslySetInnerHTML={{ __html: card.answer }} />
                            </EuiText>
                        </EuiFlexItem>
                        <EuiFlexItem />
                    </EuiFlexGroup>
                </div>
                <div className="answerButtons">
                    <EuiFlexGroup gutterSize="s" responsive={false} justifyContent="center" alignItems="flexEnd">
                        <EuiFlexItem grow={false}>
                            <EuiButton onClick={() => {
                                newCard();
                            } } color="danger">Wrong</EuiButton>
                        </EuiFlexItem>
                        <EuiFlexItem grow={false}>
                            <EuiButton onClick={() => {
                                gotCorrect()
                            }} fill color="secondary">Correct</EuiButton>
                        </EuiFlexItem>
                    </EuiFlexGroup>
                </div>
            </div>
        );
    } else return <div></div>;
};

export default Learn;
