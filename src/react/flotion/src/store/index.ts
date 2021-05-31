import {configureStore, createSlice, PayloadAction, Slice} from "@reduxjs/toolkit";
import {CardParameters, UnderstandingLevel, UserData} from "../utils";

const initialCardState: CardParameters = {
    modules: [],
    understanding: [],
};

const parameterSlice: Slice = createSlice({
    name: "parameters",
    initialState: initialCardState,
    reducers: {
        setModules: (state: CardParameters, action: PayloadAction<string[]>) => {
            state.modules = action.payload;
        },
        setUnderstandingLevels: (state: CardParameters, action: PayloadAction<UnderstandingLevel[]>) => {
            state.understanding = action.payload;
        }
    }
});

const initialUserState: UserData = {
    // @ts-ignore
    token: localStorage.getItem("flotion_token") != null ? localStorage.getItem("flotion_token") : undefined,
};

const userDataSlice: Slice = createSlice({
    name: "userData",
    initialState: initialUserState,
    reducers: {
        setUserData(state: UserData, userData: PayloadAction<UserData>) {
            state.token = userData.payload.token;
            state.firstName = userData.payload.firstName;
            state.limits = userData.payload.limits;

            if(state.token !== undefined) {
                localStorage.setItem("flotion_token", String(state.token));
            }
        },
        logoutUser(state: UserData, payload: PayloadAction<any>) {
            localStorage.clear();
            state.token = undefined;
            state.firstName = undefined;
            state.limits = undefined;
        }
    },
});

export const {
    setModules,
    setUnderstandingLevels,
} = parameterSlice.actions;

export const {
    setUserData,
    logoutUser
} = userDataSlice.actions;

export const store = configureStore({
    reducer: {
        parameters: parameterSlice.reducer,
        userData: userDataSlice.reducer
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;


