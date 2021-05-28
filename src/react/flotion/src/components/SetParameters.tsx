import React, {FC, useEffect, useState} from "react";
import {useAppDispatch, useAppSelector} from "../utils/hooks";
import {
    EuiButton,
    EuiButtonIcon,
    EuiFlexGroup,
    EuiFlexItem,
    EuiLoadingSpinner,
    EuiSpacer,
    EuiText
} from "@elastic/eui";
import "../App.css";
import {Logo} from "./Logo";
import {gql, useQuery} from "@apollo/client";
import {getHeaders} from "../utils/auth";
import {setModules, setUnderstandingLevels} from "../store";
import {isMobile} from "react-device-detect";
import {UnderstandingLevel} from "../utils";
import {useHistory} from "react-router";

const MODULES_FETCH_QUERY = gql`
    query GetModules {
        getModules {
            response
            message
            modules
            colours
        }
    }
`;

const SetParameters: FC = () => {
    const [selectionState, setSelectionState] = useState(false);

    const userData = useAppSelector((state) => state.userData);
    const parameters = useAppSelector((state) => state.parameters);

    const moduleSet = new Set<string>(parameters.modules);
    const understandingSet = new Set<UnderstandingLevel>(parameters.understanding);

    const dispatch = useAppDispatch();

    const {data, loading} = useQuery(MODULES_FETCH_QUERY, getHeaders(userData.token));
    const understandings = [UnderstandingLevel.RED, UnderstandingLevel.YELLOW, UnderstandingLevel.GREEN];

    const history = useHistory();

    if (loading) return <div className="centeredContainer"><EuiLoadingSpinner size="xl"/></div>

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

    return (
        <div className="container" style={{
            height: "100vh"
        }}>
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
                <EuiFlexItem className="fadingOpacity" id="fadeSelections" grow={true}>
                    <EuiSpacer size="xxl"/>
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
                                {!selectionState ? data.getModules.modules.map((module: string) =>
                                    <EuiFlexItem grow={false}>
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
                        <EuiFlexItem grow={isMobile}>
                            {selectionState ? <EuiButton color="warning" onClick={goToCard}>
                                Start
                            </EuiButton> : <EuiButton color="warning" onClick={toggleView}>
                                {moduleSet.size > 0 ? "Next" : "Skip"}
                            </EuiButton>}
                        </EuiFlexItem>
                        <EuiFlexItem grow={false}>
                            <EuiButtonIcon
                                iconType="boxesVertical"
                                display="base"
                                color="accent"
                                size="m"
                            />
                        </EuiFlexItem>
                    </EuiFlexGroup>
                </EuiFlexItem>
            </EuiFlexGroup>
        </div>
    );
};

export default SetParameters;
