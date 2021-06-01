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
import UserDetailsResponse from "./utils/responses";
import {useAppDispatch, useAppSelector} from "./utils/hooks";
import {setUserData} from './store';
import {makeUser} from "./utils";
import Settings from "./components/Settings";
import Loading from "./components/Loading";

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
    // First we get the viewport height and we multiple it by 1% to get a value for a vh unit
    let vh = window.innerHeight * 0.01;
    // Then we set the value in the --vh custom property to the root of the document
    document.documentElement.style.setProperty('--vh', `${vh}px`);

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
                    yellowLimit: data.userDetails.limits.yellowLimit,
                    greenLimit: data.userDetails.limits.greenLimit
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

    if(isLoading) return <Loading />;

    return (
        <div className="App">
            <Router>
            <Switch>
                <Route exact path="/">
                    { isLoggedIn ? <SetParameters /> : <Home /> }
                </Route>
                <Route path="/learn">
                    <Learn/>
                </Route>
                <Route path="/statistics">
                    <Stats/>
                </Route>
                <Route path="/auth">
                    <Authorisation/>
                </Route>
                <Route path="/error">
                    <Error/>
                </Route>
                <Route path="/settings">
                    <Settings />
                </Route>
            </Switch>
        </Router>
        </div>
    );
}

export default App;
