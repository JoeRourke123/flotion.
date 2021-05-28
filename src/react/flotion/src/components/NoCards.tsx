import {FC} from "react";
import {EuiFlexGroup, EuiFlexItem, EuiText} from "@elastic/eui";

const NoCards: FC = () => {
    return <EuiFlexGroup justifyContent="center" alignItems="center">
        <EuiFlexItem grow={false}>
            <EuiText>
                <h1>uh oh, we couldn't find any cards matching those parameters.</h1>
            </EuiText>
        </EuiFlexItem>
    </EuiFlexGroup>
};

export default NoCards;
