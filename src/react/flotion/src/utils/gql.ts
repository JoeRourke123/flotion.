import {gql} from "@apollo/client";
import {useHeaders} from "./auth";
import {CardParameters, getParameters} from "./index";

export const RANDOM_CARD_QUERY = gql`    
    query RandomCard($modules: [String!]!, $understanding: [Understanding!]!) {
        randomCard(modules: $modules, understanding: $understanding) {
            response
            message
            card {
                id
                question
                answer
                modules
                understanding
            }
        }
    }
`;

export function useRandomCardParams(token: string, parameters: CardParameters): Record<string, any> {
    return {
        ...useHeaders(token),
        variables: getParameters(parameters),
        notifyOnNetworkStatusChange: true,
        fetchPolicy: "no-cache"
    }
};

export const CORRECT_MUTATION = gql`    
    mutation CorrectCard($card: String!) {
        gotCorrect(card: $card) {
            response
            message
        }
    }
`;

export const SET_LEVELS_MUTATION = gql`
    mutation SetLevelsMutation($yellow: Int, $green: Int) {
        alterLimits(yellow: $yellow, green: $green) {
            response
            message
            limits {
                yellowLimit
                greenLimit
            }
        }
    }
`;

export const SET_MODULES_MUTATION = gql`
    mutation SetModulesMutation($modules: [String!]!) {
        setModules(modules: $modules) {
            response
            message
        }
    }
`;

export const GET_ALL_MODULES_QUERY = gql`
    query AllModules {
        allModules {
            response
            message
            modules
        }
    }
`;

export const GET_EXCLUDED_MODULES = gql`
    query ExcludedModules {
        getExcludedModules {
            response
            message
            modules
        }
    }
`;

export const MODULES_FETCH_QUERY = gql`
    query GetModules {
        getModules {
            response
            message
            modules
            colours
        }
    }
`;

export const AUTH_USER_MUTATION = gql`
    mutation AuthoriseUser($code: String!) {
        authoriseUser(code: $code) {
            response,
            message,
            user {
                firstName
                token
                limits {
                    yellowLimit
                    greenLimit
                }
            }
        }
    }
`;

export const LOGIN_LINK_QUERY = gql`
    {
        generateAuthURL
    }
`;


export const USER_DATA_QUERY = gql`
    query GetUserDetails {
        userDetails {
            response
            message
            user {
                firstName
                limits {
                    yellowLimit
                    greenLimit
                }
            }
        }
    }
`;
