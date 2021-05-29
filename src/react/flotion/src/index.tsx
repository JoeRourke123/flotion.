import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {Provider} from "react-redux";

import '@elastic/eui/dist/eui_theme_amsterdam_dark.css';

import {
    ApolloClient,
    ApolloProvider,
    createHttpLink,
    InMemoryCache
} from '@apollo/client';
import { store } from './store';
import {UNDERSTANDING_ENUM} from "./utils";

const httpLink = createHttpLink({
    uri: 'http://localhost:6969/graphql'
});

const client = new ApolloClient({
    typeDefs: [UNDERSTANDING_ENUM],
    link: httpLink,
    cache: new InMemoryCache({
        resultCaching: false,
    })
});

ReactDOM.render(
  <ApolloProvider client={client}>
      <Provider store={store}>
          <App />
      </Provider>
  </ApolloProvider>,
  document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
