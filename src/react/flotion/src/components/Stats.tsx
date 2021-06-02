import React, {FC, useEffect, useState} from "react";
import {Logo} from "./Logo";
import {EuiButtonIcon, EuiComboBox, EuiFlexGroup, EuiFlexItem, EuiSpacer, EuiStat, EuiText} from "@elastic/eui";
import {useAppSelector} from "../utils/hooks";
import {NetworkStatus, useLazyQuery, useQuery} from "@apollo/client";
import {getHeaders} from "../utils/auth";
import Loading from "./Loading";
import {useHistory} from "react-router";
import {EuiComboBoxOptionOption} from "@elastic/eui/src/components/combo_box/types";
import {getData, STATISTICS_QUERY} from "../utils/stats";
import BarChart from "./BarChart";
import {GET_ALL_MODULES_QUERY} from "../utils/gql";

const Stats: FC = () => {
    const token = useAppSelector((state) => state.userData.token);

    const [hiddenModules, setHiddenModules] = useState<string[]>([]);

    const getQueryOptions = () => {
        return {
            ...getHeaders(token),
            variables: {
                hiddenModules: hiddenModules
            },
            notifyOnNetworkStatusChange: true,
        }
    };

    const [fetchStats, { data: statData, loading, networkStatus }] = useLazyQuery(STATISTICS_QUERY, getQueryOptions());

    useEffect(() => { fetchStats(getQueryOptions()) }, []);

    const { data: moduleData, loading: moduleLoading } = useQuery(GET_ALL_MODULES_QUERY, {
        ...getHeaders(token),
    });

    const history = useHistory();

    function changeHiddenModules(modules: EuiComboBoxOptionOption[]) {
        setHiddenModules(modules.map((e) => e.label));
    }

    if(loading || moduleLoading || networkStatus === NetworkStatus.refetch) return <Loading/>;
    if((statData != null && statData.getStats.response !== 200)) {
        history.push("/error", statData.getStats);
        return <div> </div>;
    } else if(moduleData != null && moduleData.allModules.response !== 200) {
        history.push("/error", moduleData.allModules);
        return <div> </div>;
    }

    return (
        <div className="container">
            <EuiFlexGroup responsive={false} justifyContent="center" alignItems="center">
                <EuiFlexItem grow={false}>
                    <Logo />
                </EuiFlexItem>
            </EuiFlexGroup>
            <EuiSpacer />
            <EuiText>
                <h1>statistics.</h1>
            </EuiText>
            <EuiSpacer />
            <EuiFlexGroup>
                <EuiFlexItem>
                    <EuiStat title={ statData.getStats.overall.amount } description="no. of cards." />
                </EuiFlexItem>
                <EuiFlexItem>
                    <EuiStat title={ statData.getStats.moduleCount } description="no. of modules." titleColor="primary" />
                </EuiFlexItem>
                <EuiFlexItem>
                    <EuiStat title={ statData.getStats.overallRed } description="total reds." titleColor="danger" />
                </EuiFlexItem>
                <EuiFlexItem>
                    <EuiStat title={ statData.getStats.overallYellow } description="total yellows." className="yellow" titleColor="accent" />
                </EuiFlexItem>
                <EuiFlexItem>
                    <EuiStat
                        title={ statData.getStats.overallGreen }
                        description="total greens."
                        titleColor="secondary"
                    />
                </EuiFlexItem>
            </EuiFlexGroup>
            <EuiSpacer size="xxl" />
            <EuiFlexGroup>
                <EuiFlexItem>
                    <EuiComboBox
                        fullWidth
                        placeholder="select modules to hide from statistics."
                        options={ moduleData.allModules.modules.map((e: string) => {
                            return { label: e, key: e }
                        }) }
                        selectedOptions={ hiddenModules.map((v) => {
                            return { label: v }
                        }) }
                        onChange={ changeHiddenModules }
                        isClearable={true}
                        data-test-subj="hiddenModuleComboBox"
                    />
                </EuiFlexItem>
                <EuiFlexItem>
                    <EuiButtonIcon display="base" size="m" color="success" iconType="refresh" onClick={ () => {
                        fetchStats({
                            ...getHeaders(token),
                            variables: {
                                hiddenModules: hiddenModules
                            },
                        })
                    }} />
                </EuiFlexItem>
            </EuiFlexGroup>
            <EuiSpacer size="xxl"/>
            <EuiText>
                <h3>per-module stats.</h3>
            </EuiText>
            <div style={{ height: "90vh", marginTop: "-50px" }} className="chartParent">
                <BarChart data={ getData(statData) } />
            </div>
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
    );
};

export default Stats
