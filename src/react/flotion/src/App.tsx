import React, {FC, useEffect} from 'react';
import Home from './components/Home';
import {BrowserRouter as Router, Switch, Route} from 'react-router-dom';
import './App.css';
import SetParameters from "./components/SetParameters";
import Stats from "./components/Stats";
import Learn from "./components/Learn";
import Authorisation from "./components/Authorisation";
import Error from "./components/Error";
import {useAppDispatch, useAppSelector} from "./utils/hooks";
import Settings from "./components/Settings";
import {makeUser, useHeaders} from "./utils/auth";
import {useLazyQuery, useQuery} from "@apollo/client";
import {USER_DATA_QUERY} from "./utils/gql";
import UserDetailsResponse from "./utils/responses";
import {setUserData} from './store';
import Loading from "./components/Loading";

const setVH = (e: UIEvent) => {
    // First we get the viewport height and we multiple it by 1% to get a value for a vh unit
    let vh = window.innerHeight * 0.01;
    // Then we set the value in the --vh custom property to the root of the document
    document.documentElement.style.setProperty('--vh', `${vh}px`);
};

const App: FC = () => {
    window.onresize = setVH;
    setVH(new UIEvent("init"));

    const isLoggedIn = useAppSelector((state) => state.userData.token !== undefined);

    const headers = useHeaders();
    const dispatch = useAppDispatch();

    const [getUserData, { called, data, loading }] = useLazyQuery(USER_DATA_QUERY);

    useEffect(() => {
        if(isLoggedIn && !called) {
            getUserData(headers);
        } else if(data !== undefined && data != null && data.user != null) {
            let userData: UserDetailsResponse = {
                ...data.user,
                token: localStorage.getItem("flotion_token")
            };

            dispatch(
                setUserData(
                    makeUser(userData)
                )
            );
        }
    }, [data]);

    if(loading) return <Loading />;

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
