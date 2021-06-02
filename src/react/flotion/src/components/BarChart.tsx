import {ResponsiveBar} from "@nivo/bar";
import React, {FC} from "react";
import {BottomTick, GraphData, LeftTick} from "../utils/stats";

type ChartProps = { data: GraphData[] };

const BarChart: FC<ChartProps> = (props: ChartProps) => {
    return <ResponsiveBar
        data={ props.data }
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
            legend: 'cards.',
            legendPosition: 'middle',
            legendOffset: 32,
            renderTick: BottomTick
        }}
        axisLeft={{
            tickSize: 5,
            tickPadding: 5,
            tickRotation: 0,
            legend: 'module.',
            legendPosition: 'middle',
            legendOffset: -40,
            renderTick: LeftTick
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
        labelTextColor="black"
        tooltip={({ id, indexValue, value }) => (
            <strong style={{ color: "#000000" }}>
                { indexValue }: {
                id.toString().substring(0,1).toUpperCase() + id.toString().substring(1)
            } ( { value } )
            </strong>
        )}
    />
};

export default BarChart;
