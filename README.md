import React from "react";
import PropTypes from "prop-types";
import { Link } from "react-router-dom";
import "./NotFound.css";
import "../../Styles/Global.css";

export default function NotFound({ title, message, linkTo, linkText }) {
  return (
    <main className="not-found" role="main" aria-labelledby="notfound-title">
      <section className="not-found-content">
        <header>
          <h1 id="notfound-title">{title}</h1>
          <h2>{message}</h2>
        </header>

        <Link className="not-found-button" to={linkTo}>
          {linkText}
        </Link>
      </section>
    </main>
  );
}

NotFound.propTypes = {
  title: PropTypes.string,
  message: PropTypes.string,
  linkTo: PropTypes.string,
  linkText: PropTypes.string,
};

NotFound.defaultProps = {
  title: "404",
  message: "Oeps, deze pagina bestaat niet.",
  linkTo: "/login",
  linkText: "Terug naar login",
};
