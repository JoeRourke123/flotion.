type UnderstandingLimitsResponse = {
    yellowLimit: number,
    greenLimit: number
};

type UserDetailsResponse = {
    firstName: string,
    token: string,
    limits: UnderstandingLimitsResponse
};

export default UserDetailsResponse;
export const AUTH_ERROR_MSG = "Something has appeared to have gone wrong while authorising you, sorry about that! Maybe try again.";
