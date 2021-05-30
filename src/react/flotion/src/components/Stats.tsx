import React, {FC, useEffect, useState} from "react";
import {Logo} from "./Logo";
import {EuiFlexGroup, EuiFlexItem, EuiSpacer, EuiStat, EuiText} from "@elastic/eui";
import {useAppSelector} from "../utils/hooks";
import {gql, useQuery} from "@apollo/client";
import {getHeaders} from "../utils/auth";
import Loading from "./Loading";
import {useHistory} from "react-router";
import {ResponsiveBar} from "@nivo/bar";

const STATISTICS_QUERY = gql`    
    query Statistics($hiddenModules: [String!]!) {
        getStats(hiddenModules: $hiddenModules) {
            response
            message
            overall { amount }
            overallRed
            overallYellow
            overallGreen
            moduleCount
            moduleRed {
                module
                amount
            }
            moduleYellow {
                module
                amount
            }
            moduleGreen {
                module
                amount
            }
        }
    }
`;

const Stats: FC = () => {
    const token = useAppSelector((state) => state.userData.token);

    const [hiddenModules, setHiddenModules] = useState<Set<string>>(new Set());

    const { data: statData, loading } = useQuery(STATISTICS_QUERY, {
        ...getHeaders(token),
        variables: {
            hiddenModules: Array.from(hiddenModules.values())
        }
    });

    const history = useHistory();


    useEffect(() => {
        console.log(statData);
    });

    if(loading) return <Loading/>;
    if(statData != null && statData.getStats.response !== 200) {
        history.push("/error", statData.getStats);
        return <div></div>;
    }

    type GraphData = {
        module: string,
        red: number,  redColor: string,
        yellow: number, yellowColor: string,
        green: number, greenColor: string,
    };

    function getData(): GraphData[] {
        let dataList: GraphData[] = [];

        let stats = statData.getStats;

        for(let i = 0; i < statData.getStats.moduleCount; i++) {
            dataList.push({
                "module": stats.moduleRed[i].module,
                "red": stats.moduleRed[i].amount, "redColor": "hsl(3,91,68)",
                "yellow": stats.moduleYellow[i].amount, "yellowColor": "hsl(50,100,50)",
                "green": stats.moduleGreen[i].amount, "greenColor": "hsl(176,60,68)",
            })
        }

        return dataList;
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

            <div style={{ height: "90vh" }} className="chartParent">
                <ResponsiveBar
                    data={ getData() }
                    keys={[ 'red', 'yellow', 'green' ]}
                    indexBy="module"
                    margin={{ top: 50, right: 50, bottom: 50, left: 60 }}
                    padding={0.3}
                    layout="horizontal"
                    valueScale={{ type: 'linear' }}
                    indexScale={{ type: 'band', round: true }}
                    colors={ ["#F86B63", "#F3D371", "#7DDED8"] }
                    axisTop={null}
                    axisRight={null}
                    axisBottom={{
                        tickSize: 5,
                        tickPadding: 5,
                        tickRotation: 0,
                        legend: 'module',
                        legendPosition: 'middle',
                        legendOffset: 32
                    }}
                    axisLeft={{
                        tickSize: 5,
                        tickPadding: 5,
                        tickRotation: 0,
                        legend: 'stats',
                        legendPosition: 'middle',
                        legendOffset: -40
                    }}
                    labelSkipWidth={12}
                    labelSkipHeight={12}
                    legends={[
                        {
                            dataFrom: 'keys',
                            anchor: 'bottom-right',
                            direction: 'column',
                            justify: false,
                            translateX: 120,
                            translateY: 0,
                            itemsSpacing: 2,
                            itemWidth: 100,
                            itemHeight: 20,
                            itemDirection: 'left-to-right',
                            itemOpacity: 0.85,
                            symbolSize: 20,
                            effects: [
                                {
                                    on: 'hover',
                                    style: {
                                        itemOpacity: 1
                                    }
                                }
                            ]
                        }
                    ]}
                    animate={true}
                    motionStiffness={90}
                    motionDamping={15}
                />
            </div>
        </div>
    );
};

export default Stats
