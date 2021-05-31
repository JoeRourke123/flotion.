import {EuiFlexItem, EuiSpacer, EuiText} from "@elastic/eui";
import React, {FC} from "react";
import "../App.css";

type FlexStepIconProps = {
    current: number,
    step: number,
    name: string,
    title: string,
    alt: string | null
}

const FlexStepIcon: FC<FlexStepIconProps> = (props: FlexStepIconProps) => {
    const isSelected = props.current === props.step;

    return (<EuiFlexItem className="eui-fullWidth eui-textCenter">
        <div className="icons">
            <img className="iconImage" src={ `https://img.icons8.com/ios/250/FFFFFF/${ props.name }.png` } alt={props.alt != null ? props.alt : "An icon"}/>
            <img className={ (isSelected ? "iconImage selectedIcon": "iconImage unselectedIcon")} src={ `https://img.icons8.com/ios/250/FFA4D8/${ props.name }.png`} alt={props.alt != null ? props.alt : "A coloured icon"}/>
        </div>
        <EuiSpacer size="s" />
        <EuiText><h3 className={
            (isSelected ? "selectedStep" : "")
        }>{ props.title }</h3></EuiText>
    </EuiFlexItem>)
};

export default FlexStepIcon;
