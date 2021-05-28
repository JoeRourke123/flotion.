import {FC} from "react";
import {useHistory, useLocation} from "react-router";

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
        <div className="container">
            <h1>Bing bong, shit's bonked.</h1>
            { locState.response }
            { locState.message }
        </div>
    );
};

export default Error;
