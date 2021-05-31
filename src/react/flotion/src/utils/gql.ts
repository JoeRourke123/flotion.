import {gql} from "@apollo/client";

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

export const CORRECT_MUTATION = gql`    
    mutation CorrectCard($card: String!) {
        gotCorrect(card: $card) {
            response
            message
        }
    }
`;
