
// @ts-ignore
import {gql} from "@apollo/client";
import {useTheme} from "@nivo/core";
import React from "react";

export const BottomTick = (props: { x: number, y: number, value: string}) => {
    const theme = useTheme();

    return (
        <g transform={`translate(${props.x},${props.y})`}>
            <text
                textAnchor="middle"
                dominantBaseline="middle"
                style={{
                    ...theme.axis.ticks.text,
                    fill: '#DDD',
                    fontSize: 10,
                }}
            >
                {props.value}
            </text>
        </g>
    )
}
// @ts-ignore
export const LeftTick = ({ x, y, value }) => {
    const theme = useTheme();

    return (
        <g transform={`translate(${x - 30},${y})`}>
            <text
                textAnchor="middle"
                dominantBaseline="middle"
                style={{
                    ...theme.axis.ticks.text,
                    fill: '#DDD',
                    fontSize: 10,
                }}
            >
                {value}
            </text>
        </g>
    )
};

export const STATISTICS_QUERY = gql`
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

export type GraphData = {
    module: string,
    red: number,
    yellow: number,
    green: number,
};

// @ts-ignore
export function getData(statData): GraphData[] {
    let dataList: GraphData[] = [];

    let stats = statData.getStats;

    for(let i = 0; i < statData.getStats.moduleCount; i++) {
        dataList.push({
            "module": stats.moduleRed[i].module,
            "red": stats.moduleRed[i].amount,
            "yellow": stats.moduleYellow[i].amount,
            "green": stats.moduleGreen[i].amount,
        })
    }

    return dataList;
}
