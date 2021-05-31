import {FC} from "react";
import "../App.css"
import flotion from "../static/flotion_white.png";

export const Logo: FC = () => {
    return (
        <img className="logo" src={flotion} alt="Flotion logo"/>
    )
};
