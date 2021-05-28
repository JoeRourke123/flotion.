import {FC} from "react";
import {EuiFlexGroup, EuiFlexItem, EuiText} from "@elastic/eui";
import React from "react";

const NoCards: FC = () => {
    return <div className="centeredContainer">
        <h1>uh oh, we couldn't find any cards matching those parameters.</h1>
    </div>
};

export default NoCards;
