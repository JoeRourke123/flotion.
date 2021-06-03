import UserDetailsResponse from "./responses";
import {UnderstandingLevel} from "./index";
import {useHistory} from "react-router";
import {useAppSelector} from "./hooks";

//@ts-ignore
function checkResponse(data, history): void {
    if(data.hasOwnProperty("response") && data.response >= 400) {
        history.replace("/error", data);
    }
}

export const useHeaders = (token?: string) => {
    const history = useHistory();
    const userToken = useAppSelector((state) => state.userData.token);

    return {
        context: {
            headers: {
                "Authorization": userToken
            }
        },
        onCompleted: (d: any) => { checkResponse(d[Object.keys(d)[0]], history) },
        onError: (e: any) => {
            if(history !== undefined) {
                history.replace("/error");
            } else if(window.location.pathname !== "/error") {
                window.location.replace("/error");
            }
        }
    }
};

export type UserData = {
    firstName?: string,
    token?: String,
    limits?: {
        [UnderstandingLevel.RED]: 0,
        [UnderstandingLevel.YELLOW]: number,
        [UnderstandingLevel.GREEN]: number
    }
}

export function makeUser(responseData: UserDetailsResponse): UserData {
    return {
        firstName: responseData.firstName,
        token: responseData.token,
        limits: {
            [UnderstandingLevel.RED]: 0,
            [UnderstandingLevel.YELLOW]: responseData.limits.yellowLimit,
            [UnderstandingLevel.GREEN]: responseData.limits.greenLimit
        }
    }
}
