import React, {FC, useEffect, useState} from 'react';
import Home from './components/Home';
import {BrowserRouter as Router, Switch, Route} from 'react-router-dom';
import './App.css';
import SetParameters from "./components/SetParameters";
import Stats from "./components/Stats";
import Learn from "./components/Learn";
import Authorisation from "./components/Authorisation";
import Error from "./components/Error";
import {gql, useLazyQuery} from "@apollo/client";
import {EuiLoadingSpinner} from "@elastic/eui";
import UserDetailsResponse from "./utils/responses";
import {useAppDispatch, useAppSelector} from "./utils/hooks";
import {setUserData} from './store';
import {makeUser} from "./utils";

const USER_DATA_QUERY = gql`
    query GetUserDetails {
        userDetails {
            firstName
            limits {
                yellowLimit
                greenLimit
            }
        }
    }
`;

const App: FC = () => {
    const dispatch = useAppDispatch();
    const userToken = useAppSelector((state) => state.userData.token);

    const token = localStorage.getItem("flotion_token");
    const [ isLoggedIn, setLoggedIn ] = useState(false);
    const [ isLoading, setLoading ] = useState(false);
    const [getUserDetails, { data }] = useLazyQuery(USER_DATA_QUERY);

    useEffect(() => {
        if(token != null && !isLoading) {
            getUserDetails({
                context: {
                    headers: {
                        "Authorization": token
                    }
                }
            });
            setLoading(true);
        }

        if(data && token != null) {
            let userResponse: UserDetailsResponse = {
                firstName: data.userDetails.firstName,
                token: token,
                limits: {
                    yellowLimit: data.userDetails.yellowLimit,
                    greenLimit: data.userDetails.greenLimit
                }
            };


            dispatch(setUserData(makeUser(userResponse)));
            setLoggedIn(true);
            setLoading(false);
        }
    }, [data]);

    useEffect(() => {
        setLoggedIn(userToken !== undefined);
    }, [userToken]);

    return (
        <div className="App">
            {(isLoading ? <div className="centeredContainer">
            <EuiLoadingSpinner size="xl" />
        </div> : <Router>
            <Switch>
                <Route exact path="/">
                    { isLoggedIn ? <SetParameters /> : <Home /> }
                </Route>
                <Route path="/learn">
                    <Learn/>
                </Route>
                <Route path="/stats">
                    <Stats/>
                </Route>
                <Route path="/auth">
                    <Authorisation/>
                </Route>
                <Route path="/error">
                    <Error/>
                </Route>
            </Switch>
        </Router>)}
        </div>
    );
}

export default App;
