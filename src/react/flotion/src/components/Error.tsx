import {FC} from "react";
import {useHistory, useLocation} from "react-router";
import {EuiButton, EuiFlexGroup, EuiFlexItem, EuiSpacer, EuiText} from "@elastic/eui";
import {useAppDispatch} from "../utils/hooks";
import {logoutUser} from "../store";

const Error: FC = () => {
    const location = useLocation();
    const history = useHistory();

    // @ts-ignore
    const locState: { response: number, message: string } = location.state !== undefined ? location.state : {
        response: 500,
        message: "umm, it looks like we've messed up somewhere - sorry about that. if it keeps happening, try signing out and back in again."
    };

    const dispatch = useAppDispatch();

    const goHome = () => {
        if(history !== undefined) {
            history.replace("/");
        } else {
            window.location.href = "/";
        }
    };

    // @ts-ignore
    return (
        <div className="centeredContainer">
            <EuiText>
                <h1>i don't believe it! there was an error...</h1>
            </EuiText>
            <EuiSpacer />
            <EuiText textAlign="left">
                <h3>what we know is:</h3>
                <span>(code { locState.response }): "{ locState.message }"</span>
            </EuiText>
            <EuiSpacer />
            <div>
                <EuiButton fill color="secondary" onClick={ goHome } style={{ marginRight: "20px"}}>
                    go back home.
                </EuiButton>

                <EuiButton color="danger" onClick={ () => {
                    localStorage.clear();
                    dispatch(logoutUser(null));
                    goHome();
                }}>sign out.</EuiButton>
            </div>
        </div>
    );
};

export default Error;
