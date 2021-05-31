import { EuiButton, EuiFlexGroup, EuiFlexItem, EuiSpacer, EuiText, EuiTextColor} from "@elastic/eui";
import React, {FC, useState} from "react";
import "../App.css";
import {Logo} from "./Logo";
import FlexStepIcon from "./StepIcon";
import {gql, useQuery} from "@apollo/client";

const LOGIN_LINK_QUERY = gql`
{
    generateAuthURL
}
`;

const Home: FC = (props) => {
    const [step, setStep] = useState(0);
    const { loading, data } = useQuery(LOGIN_LINK_QUERY);

    const stepText = [
        <EuiFlexItem id="step0" className="stepDescription" grow={false}>
            <EuiText>to get started with flotion, you need to copy our template (you can find it <EuiTextColor color="accent">
                <a style={{color: "var(--pink)"}}
                    href="https://www.notion.so/14c1b95ff6ee4086b9232541e855d818">here!</a>
            </EuiTextColor>) be sure not to remove/change
                any of the database structure or the app may break.</EuiText>
            <EuiSpacer/>
            <EuiButton onClick={() => progressStep()} color="secondary" fill>i've done this!</EuiButton>
        </EuiFlexItem>,
        <EuiFlexItem className="stepDescription" grow={false}>
            <EuiText>now you need to authorise your account with flotion. if you're logging in for the first time, be
                sure to check your newly created 'flotion' page when prompted.</EuiText>
            <EuiSpacer/>
            <EuiButton isLoading={loading} onClick={() => openLink()} color="secondary" fill>authorise me.</EuiButton>
        </EuiFlexItem>,
    ];

    function progressStep() {
        let container = document.getElementById("stepDescriptionContainer");
        // @ts-ignore
        container.style.opacity = "0";

        setTimeout(() => {
            setStep(step + 1);
            // @ts-ignore
            container.style.opacity = "1";
        }, 600);
    }

    function openLink() {
        window.location.assign(data.generateAuthURL);
    }

    return (
        <div className="container">
            <div className="row">
                <Logo/>
            </div>
            <EuiSpacer size="xl" />
            <EuiText>
                <h1>getting started with flotion...</h1>
            </EuiText>
            <EuiSpacer/>
            <EuiFlexGroup>
                <FlexStepIcon name="box" alt="Line drawing of a box, icon." title="copy the template." current={step} step={0}  />
                <FlexStepIcon name="unlock" alt="Line drawing icon of an unlocked padlock." title="authorise your account." current={step} step={1}  />
                <FlexStepIcon name="brain" alt="Line drawing icon of a brain." title="start learning." current={step} step={2}  />
            </EuiFlexGroup>
            <EuiSpacer size="xl" />
            <EuiSpacer size="xl" />
            <EuiFlexGroup id="stepDescriptionContainer" style={{minHeight: "128px"}} justifyContent="spaceAround">
                { stepText[step] }
            </EuiFlexGroup>
        </div>
    );
};

export default Home;
