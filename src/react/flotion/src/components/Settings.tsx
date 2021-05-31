import {FC, useEffect, useState} from "react";
import React from "react";
import {
    EuiButton,
    EuiButtonIcon,
    EuiFieldNumber,
    EuiFlexGroup,
    EuiFlexItem,
    EuiGlobalToastList,
    EuiSelectable, EuiSelectableOption,
    EuiSpacer,
    EuiText
} from "@elastic/eui";
import {Logo} from "./Logo";
import {useAppSelector} from "../utils/hooks";
import {UnderstandingLevel} from "../utils";
import {gql, useMutation, useQuery} from "@apollo/client";
import {getHeaders} from "../utils/auth";
import {Toast} from "@elastic/eui/src/components/toast/global_toast_list";
import Loading from "./Loading";
import {useHistory} from "react-router";

const SET_LEVELS_MUTATION = gql`    
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

const SET_MODULES_MUTATION = gql`    
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

const GET_EXCLUDED_MODULES = gql`    
    query ExcludedModules {
        getExcludedModules {
            response
            message
            modules
        }
    }
`;

const Settings: FC = () => {
    const initialLimits = useAppSelector((state) => state.userData.limits);
    const token = useAppSelector((state) => state.userData.token);

    const history = useHistory();

    const [greenLimit, setGreenLimit] = useState(initialLimits[UnderstandingLevel.GREEN]);
    const [yellowLimit, setYellowLimit] = useState(initialLimits[UnderstandingLevel.YELLOW]);
    const [toasts, setToasts] = useState<Toast[]>([]);

    const [setLevels] = useMutation(SET_LEVELS_MUTATION);
    const [saveExcludedModules] = useMutation(SET_MODULES_MUTATION);
    const { data: allModulesData, loading: moduleLoading, } = useQuery(GET_ALL_MODULES_QUERY, { ...getHeaders(token) });
    const {data: excludedData, loading: excludedLoading } = useQuery(GET_EXCLUDED_MODULES, {
        ...getHeaders(token),
    });

    const [excludedSet, setExcludedModules]: [Set<string>, React.Dispatch<React.SetStateAction<Set<string>>>] = useState<Set<string>>(new Set());

    function getModuleOptions(): EuiSelectableOption[] {
        if(allModulesData == null || excludedData == null) return [];

        return allModulesData.allModules.modules.map((e: string) => {
            return {
                label: e,
                checked: excludedSet.has(e) ? 'on' : undefined
            }
        });
    }

    useEffect(() => {
        if(excludedData != null) {
            setExcludedModules(new Set(excludedData.getExcludedModules.modules))
        }
    }, [excludedData]);

    function changedExcludedModules(modules: EuiSelectableOption[]) {
        let excludedModules = [];

        for(let module of modules) {
            if(module.checked === "on") {
                excludedModules.push(module.label);
            }
        }

        setExcludedModules(new Set(excludedModules));
    }

    const removeToast = (removedToast: Toast) => {
        setToasts(toasts.filter((toast: Toast) => toast.id !== removedToast.id));
    };

    function validLimits(yellow: number, green: number): boolean {
        return yellow != null && green != null && yellow > 0 && green > yellow;
    }

    function saveLimits() {
        if(yellowLimit !== initialLimits[UnderstandingLevel.YELLOW] ||
            greenLimit !== initialLimits[UnderstandingLevel.GREEN]) {
            if(validLimits(yellowLimit, greenLimit)) {
                setLevels({
                    ...getHeaders(token),
                    variables: {
                        yellow: yellowLimit,
                        green: greenLimit,
                    }
                }).then((resp) => {
                    if(resp.data.alterLimits.response !== 200) {
                        history.replace("/error", resp.data.alterLimits);
                    } else {
                        setToasts([...toasts, {
                            id: `${toasts.length}`,
                            title: "your understanding limits have been updated!",
                            color: "success",
                            iconType: "check"
                        }]);
                    }
                });
            }
        }
    }

    function saveModules() {
        saveExcludedModules({
            ...getHeaders(token),
            variables: {
                modules: Array.from(excludedSet.values())
            }
        }).then((resp) => {
            if(resp.data.setModules.response !== 200) {
                history.replace("/error", resp.data.setModules);
            } else {
                setToasts([...toasts, {
                    id: `${toasts.length}`,
                    title: "your modules to hide have been saved!",
                    color: "success",
                    iconType: "check"
                }]);
            }
        });
    }

    function setToDefault() {
        setGreenLimit(DEFAULTS[UnderstandingLevel.GREEN]);
        setYellowLimit(DEFAULTS[UnderstandingLevel.YELLOW]);
        saveLimits();
    }

    const DEFAULTS = {
        [UnderstandingLevel.RED]: 0,
        [UnderstandingLevel.YELLOW]: 3,
        [UnderstandingLevel.GREEN]: 5,
    };

    if (excludedLoading || moduleLoading) return <Loading/>;
    else return <div className="container">
        <EuiFlexGroup responsive={false} justifyContent="center" alignItems="center">
            <EuiFlexItem grow={false}>
                <Logo />
            </EuiFlexItem>
        </EuiFlexGroup>
        <EuiSpacer size="xl" />
        <EuiText>
            <h1>settings.</h1>
            <h3>understanding limits.</h3>
        </EuiText>
        <EuiSpacer/>
        <EuiFieldNumber
            placeholder="when cards will be marked as 'yellow'"
            value={ yellowLimit }
            onChange={ (v) => { setYellowLimit(v.target.value) }}
            prepend="yellow."
            min={1}
            max={greenLimit - 1}
        />
        <EuiSpacer />
        <EuiFieldNumber
            placeholder="when cards will be marked as 'green'"
            value={ greenLimit }
            onChange={ (v) => { setGreenLimit(v.target.value) } }
            prepend="green."
            min={yellowLimit + 1}
        />
        <EuiSpacer/>
        <EuiFlexGroup gutterSize="s" responsive={false}>
            <EuiFlexItem grow={false}>
                <EuiButton fill color="secondary" onClick={() => setToDefault() }>
                    reset to default.
                </EuiButton>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
                <EuiButtonIcon display="base" size="m" color="warning" iconType="save" onClick={ () => saveLimits() } isDisabled={ !validLimits(yellowLimit, greenLimit) } />
            </EuiFlexItem>
        </EuiFlexGroup>
        <EuiSpacer size="xxl" />
        <EuiSpacer size="xxl" />
        <EuiText>
            <h3>hidden modules.</h3>
        </EuiText>
        <EuiSpacer/>
        <EuiSelectable
            aria-label="Selection of "
            options={ getModuleOptions() }
            listProps={{ showIcons: true }}
            onChange={changedExcludedModules}>
            {(list) => list}
        </EuiSelectable>
        <EuiSpacer/>
        <EuiFlexGroup gutterSize="s" responsive={false}>
            <EuiFlexItem grow={false}>
                <EuiButton fill color="secondary" onClick={() => setExcludedModules(new Set()) }>
                    un-exclude all.
                </EuiButton>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
                <EuiButtonIcon display="base" size="m" color="warning" iconType="save" onClick={ () => saveModules() } />
            </EuiFlexItem>
        </EuiFlexGroup>
        <EuiGlobalToastList
            toasts={toasts}
            dismissToast={removeToast}
            toastLifeTimeMs={6000}
        />
        <EuiButtonIcon
            color="ghost"
            size="m"
            iconType="arrowLeft"
            onClick={() => history.goBack() }
            style={{
                position: "absolute",
                top: "20px",
                left: "10px"
            }}
        />
    </div>
};

export default Settings;
