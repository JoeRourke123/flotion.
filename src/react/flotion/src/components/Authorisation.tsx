import React, {FC, useEffect, useState} from "react";
import {EuiButton, EuiLoadingSpinner, EuiSpacer, EuiText} from "@elastic/eui";
import {gql, useMutation} from "@apollo/client";
import {useHistory, useLocation} from "react-router";
import {makeUser, UserData} from "../utils";
import UserDetailsResponse, {AUTH_ERROR_MSG} from "../utils/responses";
import {useAppDispatch} from "../utils/hooks";
import {setUserData} from "../store";

const AUTH_USER_MUTATION = gql`
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

const Authorisation: FC = (props) => {
    const search = useLocation().search;
    const authCode = new URLSearchParams(search).get('code');
    const authError = new URLSearchParams(search).get('error');
    const routerHistory = useHistory();

    const [authoriseUser] = useMutation(AUTH_USER_MUTATION, {
        variables: {
            code: authCode
        }
    });

    const [isLoading, setIsLoading] = useState(true);
    const [success, setIsSuccess] = useState(true);
    const [userInfo, setUserInfo] = useState<UserData>();

    const dispatch = useAppDispatch();


    useEffect(() => {
        if(authError !== null) {
            routerHistory.push("/error", {
                response: 400,
                message: AUTH_ERROR_MSG
            });
            return;
        }

        authoriseUser().then((e) => {
            setIsLoading(false);
            setIsSuccess(e.data.authoriseUser.response === 200);

            if(success) {
                let responseData: UserDetailsResponse = e.data.authoriseUser.user;

                if(responseData == null) {
                    routerHistory.push("/error");
                } else {
                    let parsedUser = makeUser(responseData);

                    dispatch(setUserData(parsedUser));
                    setUserInfo(parsedUser);
                }

            } else {
                routerHistory.push("/error", {
                    response: e.data.authoriseUser.response,
                    message: e.data.authoriseUser.message
                })
            }
        });
    }, []);

    function setDetails() {
        routerHistory.push("/learn");
    }

    return isLoading || userInfo == null ? (
      <div className="centeredContainer">
          <EuiLoadingSpinner size="xl" />
      </div>
    ) : (
        <div className="centeredContainer">
            <EuiText>
                <h1>congratulations! you're ready to use flotion.</h1>
                <span>welcome { userInfo.firstName?.toLowerCase() }, to the easiest way to make and use flashcards.</span>
            </EuiText>
            <EuiSpacer />
            <EuiButton fill onClick={() => setDetails()} color="secondary">
                shall we begin?
            </EuiButton>
        </div>
    );
};

export default Authorisation;
