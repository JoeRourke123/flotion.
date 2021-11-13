import React, {FC, useEffect, useState} from "react";
import {useAppDispatch, useAppSelector} from "../utils/hooks";
import {useHistory} from "react-router";
import {NetworkStatus, useLazyQuery, useMutation, useQuery} from "@apollo/client";
import {CORRECT_MUTATION, useRandomCardParams, RANDOM_CARD_QUERY} from "../utils/gql";
import {Flashcard, toggleDrawingWindow, toggleFlashcard} from "../utils/flashcard";
import NoCards from "./NoCards";
import {
    EuiButton, EuiButtonIcon, EuiContextMenu, EuiFlexGroup, EuiFlexItem, EuiIcon,
    EuiPopover, EuiText
} from "@elastic/eui";
import {useHeaders} from "../utils/auth";
import {getParameters, getUnderstanding, UnderstandingLevel} from "../utils";
import "../App.css";
import {isMobile} from "react-device-detect";
import CanvasDraw from "react-canvas-draw";
import {logoutUser} from "../store";
import Loading from "./Loading";

const Learn: FC = () => {
    const token = useAppSelector((state) => state.userData.token);
    const parameters = useAppSelector((state) => state.parameters);
    const dispatch = useAppDispatch();

    const headers = useHeaders(token);

    const [canvas, setCanvas] = useState<CanvasDraw>();
    const [card, setCard] = useState<Flashcard>();
    const [isPopoverOpen, setPopoverOpen] = useState(false);
    const [showDrawing, setShowDrawing] = useState(false);
    const [showQuestion, setShowQuestion] = useState(true);

    const {data, loading, refetch, networkStatus, error} = useQuery(RANDOM_CARD_QUERY, useRandomCardParams(token, parameters));
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

    function isLoading() {
        return loading || networkStatus === NetworkStatus.refetch;
    }

    async function newCard() {
        if(!showQuestion) {
            toggleFlashcard(showQuestion, setShowQuestion);
        }
        await refetch();
    }

    async function gotCorrect() {
        newCard();
        markAsCorrect({
            variables: { card: card?.id },
            ...headers
        });
    }

    function resetDrawing() {
        if(canvas !== undefined) {
            canvas.clear();
        }
    }

    useEffect(() => {
        if(error != null) {
            history.replace("/error", data.randomCard);
            return;
        }

        if (data !== undefined) {
            if (data.randomCard.response >= 400) {
                history.replace("/error", data.randomCard);
            } else if (data.randomCard.card != null) {
                let cardData = {...data.randomCard.card};
                cardData.understanding = getUnderstanding(cardData.understanding);

                const flashcard: Flashcard = cardData;
                setCard(flashcard);
            }
        }
    }, [data, error]);

    const panels = [
        {
            id: 0,
            items: [
                {
                    name: 'Back to Home',
                    icon: 'home',
                    onClick: () => {
                        if(history.length > 1) {
                            history.goBack();
                        } else {
                            history.replace("/");
                        }
                    }
                },
                {
                    name: 'Settings',
                    icon: 'gear',
                    onClick: () => {
                        history.push("/settings");
                    }
                },
                {
                    name: 'Statistics',
                    icon: 'visualizeApp',
                    onClick: () => {
                        history.push("/statistics");
                    }
                },
                {
                    name: 'Sign Out',
                    icon: <EuiIcon type="exit" size="m" color="danger" />,
                    onClick: () => {
                        dispatch(logoutUser(null));
                        history.go(history.length - 1);
                        history.replace("/");
                    }
                },
            ],
        },
    ];

    if (isLoading()) return <Loading/>;
    if (data != null && data.randomCard.response === 204) {
        return <NoCards/>
    } else if (card != null) {
        return (
            <div style={{ width: "100%", minHeight: "calc(var(--vh, 1vh) * 100)", overflowX: "hidden", }}>
                <EuiButtonIcon
                    color="ghost"
                    className="drawingButton"
                    onClick={() => { toggleDrawingWindow(showDrawing, setShowDrawing) }}
                    size="m"
                    iconType="pencil"
                    aria-label="Draw"
                />
                <EuiPopover
                    className="moreButton"
                    button={<EuiButtonIcon
                        color="ghost"
                        onClick={() => { setPopoverOpen(!isPopoverOpen) }}
                        size="m"
                        iconType="boxesVertical"
                        aria-label="Draw"
                    />}
                    isOpen={isPopoverOpen}
                    closePopover={ () => { setPopoverOpen(false) }}
                    panelPaddingSize="m"
                    anchorPosition="leftUp">
                    <EuiContextMenu initialPanelId={0} panels={panels} />
                </EuiPopover>
                <div id="drawing" className="eui-fullHeight drawing">
                    <EuiButtonIcon
                        color="accent"
                        className="drawingButton"
                        onClick={() => { toggleDrawingWindow(showDrawing, setShowDrawing) }}
                        size="m"
                        iconType="cross"
                        aria-label="Hide"
                        style={{ zIndex: 9999 }}
                    />
                    <EuiButtonIcon
                        color="accent"
                        className="moreButton"
                        onClick={() => { resetDrawing() }}
                        size="m"
                        iconType="trash"
                        aria-label="Discard"
                        style={{ zIndex: 9999 }}
                    />
                    <CanvasDraw
                        hideInterface={true}
                        immediateLoading={true}
                        backgroundColor="#FFFFFF"
                        brushRadius={2}
                        lazyRadius={0}
                        canvasWidth={ window.innerWidth }
                        canvasHeight={ window.innerHeight }
                        ref={ c => (setCanvas(c != null ? c : undefined)) }
                    />
                </div>
                <div id="question" className="eui-fullHeight question" onClick={() => isPopoverOpen ? null : toggleFlashcard(showQuestion, setShowQuestion) }>

                    <EuiFlexGroup responsive={false} className="eui-fullHeight" direction="column" justifyContent="center"
                                  alignItems="center">
                        <EuiFlexItem grow={true} />
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
                                            card.modules.map((e) => <EuiFlexItem key={ e } grow={false}>
                                                <EuiButton size="s">
                                                    {e}
                                                </EuiButton>
                                            </EuiFlexItem>)
                                        }
                                    </EuiFlexGroup>
                                </EuiFlexItem>
                            </EuiFlexGroup>
                        </EuiFlexItem>
                        <EuiFlexItem grow={true} />
                        <EuiFlexItem grow={false}>
                            <span style={{color: "#AAAAAA"}}>click anywhere to show answer.</span>
                        </EuiFlexItem>
                    </EuiFlexGroup>
                </div>
                <div id="answer" style={{ display: "none" }} onClick={() => isPopoverOpen ? null : toggleFlashcard(showQuestion, setShowQuestion)}>
                    <div className="answerContents">
                        <EuiFlexGroup className="contentsHeight" responsive={false} direction="column" justifyContent="center"
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
                </div>
                <div className="answerButtons">
                    <EuiFlexGroup gutterSize="s" responsive={false} justifyContent="center" alignItems="flexEnd">
                        <EuiFlexItem grow={isMobile}>
                            <EuiButton onClick={() => {
                                newCard();
                            } } color="danger">Wrong</EuiButton>
                        </EuiFlexItem>
                        <EuiFlexItem grow={isMobile}>
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
