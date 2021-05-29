import {FC} from "react";
import {useHistory, useLocation} from "react-router";
import {EuiButton, EuiSpacer, EuiText} from "@elastic/eui";

const Error: FC = () => {
    const location = useLocation();
    const history = useHistory();

    // @ts-ignore
    const locState: { response: number, message: string } = location.state !== undefined ? location.state : {
        response: 500,
        message: "umm, it looks like we've messed up somewhere - sorry about that."
    };

    // @ts-ignore
    return (
        <div className="centeredContainer">
            <EuiText>
                <h1>well, this is awkward... there was an error.</h1>
            </EuiText>
            <EuiSpacer />
            <EuiText textAlign="left">
                <h3>what we know is:</h3>
                <span>(code { locState.response }): "{ locState.message }"</span>
            </EuiText>
            <EuiSpacer />
            <EuiButton fill color="secondary" onClick={ () => { history.replace("/") }}>
                go back home.
            </EuiButton>
        </div>
    );
};

export default Error;
