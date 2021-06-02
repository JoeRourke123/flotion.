import UserDetailsResponse from "./responses";
import {UnderstandingLevel} from "./index";

export const getHeaders = (token: string) => {
    return {
        context: {
            headers: {
                "Authorization": token
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
