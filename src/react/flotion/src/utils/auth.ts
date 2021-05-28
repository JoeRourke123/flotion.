export const getHeaders = (token: string) => {
    return {
        context: {
            headers: {
                "Authorization": token
            }
        }
    }
};
