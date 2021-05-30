import React, {FC} from "react";
import {EuiLoadingSpinner} from "@elastic/eui";

const Loading: FC = () => {
    return <div className="centeredContainer"><EuiLoadingSpinner size="xl"/></div>;
};

export default Loading;
