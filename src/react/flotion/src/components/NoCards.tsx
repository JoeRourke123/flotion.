import {FC} from "react";
import {EuiButton, EuiSpacer, EuiText} from "@elastic/eui";
import React from "react";
import {useHistory} from "react-router";

const NoCards: FC = () => {
    const history = useHistory();

    return <div className="centeredContainer">
        <EuiText>
            <h1>uh oh, we couldn't find any cards matching those parameters.</h1>
        </EuiText>
        <EuiSpacer />
        <EuiButton fill color="secondary" onClick={() => { history.goBack() }}>
            change parameters.
        </EuiButton>
    </div>
};

export default NoCards;
