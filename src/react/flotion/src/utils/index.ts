import UserDetailsResponse from "./responses";
import {gql} from "@apollo/client";

export enum UnderstandingLevel {
    RED,
    YELLOW,
    GREEN
}

export const UNDERSTANDING_ENUM = gql`
    enum Understanding {
        GREEN
        RED
        YELLOW
    }
`;

export function getUnderstanding(understanding: string): UnderstandingLevel {
    // @ts-ignore
    return {
        "RED": UnderstandingLevel.RED,
        "YELLOW": UnderstandingLevel.YELLOW,
        "GREEN": UnderstandingLevel.GREEN,
    }[understanding];
}

export type CardParameters = {
    modules: string[],
    understanding: UnderstandingLevel[]
}

export function getParameters(parameters: CardParameters): { modules: string[], understanding: string[] } {
    return {
        modules: parameters.modules,
        understanding: parameters.understanding.map((e) => ["RED", "YELLOW", "GREEN"][e]),
    }
}

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
