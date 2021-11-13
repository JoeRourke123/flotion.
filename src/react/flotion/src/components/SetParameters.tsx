import React, {FC, useEffect, useState} from "react";
import {useAppDispatch, useAppSelector} from "../utils/hooks";
import {
    EuiButton,
    EuiButtonIcon, EuiContextMenu,
    EuiFlexGroup,
    EuiFlexItem,
    EuiIcon,
    EuiLoadingSpinner,
    EuiPopover,
    EuiSpacer,
    EuiText
} from "@elastic/eui";
import "../App.css";
import {Logo} from "./Logo";
import {useQuery} from "@apollo/client";
import {useHeaders} from "../utils/auth";
import {logoutUser, setModules, setUnderstandingLevels} from "../store";
import {UnderstandingLevel} from "../utils";
import {useHistory} from "react-router";
import {MODULES_FETCH_QUERY} from "../utils/gql";

const SetParameters: FC = () => {
    const [selectionState, setSelectionState] = useState(false);

    const [isPopoverOpen, setPopoverOpen] = useState(false);

    const userData = useAppSelector((state) => state.userData);
    const parameters = useAppSelector((state) => state.parameters);

    const moduleSet = new Set<string>(parameters.modules);
    const understandingSet = new Set<UnderstandingLevel>(parameters.understanding);

    const dispatch = useAppDispatch();

    const {data, loading, refetch} = useQuery(MODULES_FETCH_QUERY, {
        ...useHeaders(userData.token),
        fetchPolicy: "network-only",
        nextFetchPolicy: "network-only"
    })

    const understandings = [UnderstandingLevel.RED, UnderstandingLevel.YELLOW, UnderstandingLevel.GREEN];

    const history = useHistory();

    if (loading) return <div className="centeredContainer"><EuiLoadingSpinner size="xl"/></div>

    const loadedModulesSet = new Set<string>(data.getModules.modules);
    moduleSet.forEach((module) => {
        if (!loadedModulesSet.has(module)) {
            toggle(module, moduleSet);
        }
    })


    function toggle(item: string | UnderstandingLevel, set: Set<string | UnderstandingLevel>) {
        let copySet = new Set<string | UnderstandingLevel>(set);

        if (copySet.has(item)) {
            copySet.delete(item);
        } else {
            copySet.add(item);
        }

        if (selectionState) {
            dispatch(setUnderstandingLevels(Array.from(copySet.values())));
        } else {
            dispatch(setModules(Array.from(copySet.values())))
        }
    }

    function toggleView() {
        const selections = document.getElementById("fadeSelections");
        const buttons = document.getElementById("fadeButtons");

        if (selections != null) selections.style.opacity = "0";
        if (buttons != null) buttons.style.opacity = "0";

        setTimeout(() => {
            setSelectionState(!selectionState);

            if (selections != null) selections.style.opacity = "1";
            if (buttons != null) buttons.style.opacity = "1";
        }, 550);
    }

    function goToCard() {
        history.push("/learn");
    }

    const panels = [
        {
            id: 0,
            items: [
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
                        history.push("/statistics", {
                            date: new Date()
                        });
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

    // @ts-ignore
    return (
        <div className="container parameters">
            <EuiFlexGroup responsive={false} direction="column" style={{
                height: "100%"
            }}>
                <EuiFlexItem grow={false}>
                    <EuiFlexGroup responsive={false} justifyContent="center">
                        <EuiFlexItem grow={false}>
                            <Logo/>
                        </EuiFlexItem>
                    </EuiFlexGroup>
                </EuiFlexItem>
                <EuiFlexItem />
                <EuiFlexItem className="fadingOpacity" id="fadeSelections" grow={true}>
                    <EuiFlexGroup responsive={false} direction="column" justifyContent="center">
                        <EuiFlexItem>
                            <EuiFlexGroup style={{
                                textAlign: "center"
                            }} alignItems="center" justifyContent="center">
                                <EuiFlexItem grow={false}>
                                    <EuiText className="fadingElement" id="fadingTitle">
                                        <h1>{
                                            !selectionState ? "select modules." : "select understanding."
                                        }</h1>
                                    </EuiText>
                                </EuiFlexItem>
                            </EuiFlexGroup>
                        </EuiFlexItem>
                        <EuiFlexItem>
                            <EuiFlexGroup gutterSize="s" alignItems="center" justifyContent="center">
                                { (!selectionState && (data.getModules.modules === null || data.getModules.modules.length === 0)) ? <EuiText>
                                    <h3>it appears you have no modules set up in Notion. make sure you haven't excluded all your modules in the app settings.</h3>
                                </EuiText> : <span></span>}
                                {!selectionState && data.getModules.modules != null ? data.getModules.modules.map((module: string) =>
                                    <EuiFlexItem grow={false} key={ module }>
                                        <EuiButton
                                            color="secondary"
                                            fill={moduleSet.has(module)}
                                            onClick={() => toggle(module, moduleSet)}
                                        >
                                            {module}
                                        </EuiButton>
                                    </EuiFlexItem>) : understandings.map((u: UnderstandingLevel) =>
                                    <EuiFlexItem grow={false}>
                                        <EuiButton
                                            key={ ["Red", "Yellow", "Green"][u] }
                                            color="secondary"
                                            fill={understandingSet.has(u)}
                                            onClick={() => toggle(u, understandingSet)}
                                        >
                                            {["Red", "Yellow", "Green"][u]}
                                        </EuiButton>
                                    </EuiFlexItem>)
                                }
                            </EuiFlexGroup>
                        </EuiFlexItem>
                    </EuiFlexGroup>
                    <EuiSpacer size="xxl"/>
                </EuiFlexItem>
                <EuiFlexItem grow={true}/>
                <EuiFlexItem className="fadingOpacity" id="fadeButtons" grow={false}>
                    <EuiFlexGroup responsive={false} gutterSize="xs" justifyContent="center">
                        {selectionState ? <EuiFlexItem grow={false}>
                            <EuiButtonIcon
                                onClick={toggleView}
                                iconType="arrowLeft"
                                display="base"
                                color="primary"
                                size="m"
                            />
                        </EuiFlexItem> : ''}
                        <EuiFlexItem grow={false}>
                            {selectionState ? <EuiButton color="warning" onClick={goToCard}>
                                Start
                            </EuiButton> : <EuiButton color="warning" onClick={toggleView}>
                                {moduleSet.size > 0 ? "Next" : "Skip"}
                            </EuiButton>}
                        </EuiFlexItem>
                        <EuiFlexItem grow={false}>
                            <EuiPopover
                                button={<EuiButtonIcon
                                    iconType="boxesVertical"
                                    display="base"
                                    color="accent"
                                    size="m"
                                    onClick={() => { setPopoverOpen(!isPopoverOpen) }}
                                />}
                                isOpen={isPopoverOpen}
                                closePopover={ () => { setPopoverOpen(false) }}
                                panelPaddingSize="m"
                                anchorPosition="upCenter">
                                <EuiContextMenu initialPanelId={0} panels={panels} />
                            </EuiPopover>
                        </EuiFlexItem>
                    </EuiFlexGroup>
                </EuiFlexItem>
            </EuiFlexGroup>
        </div>
    );
};

export default SetParameters;
