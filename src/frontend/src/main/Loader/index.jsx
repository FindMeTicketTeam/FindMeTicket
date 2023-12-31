import React from 'react';
import './loader.css';

export default function Loader() {
  return (
    <div data-testid="loader" className="loader-wrapper">
      <div className="truck-wrapper" data-content="Наш сервіс вже шукає квитки для вас">
        <div className="truck">
          <div className="truck-container" />
          <div className="glases" />
          <div className="bonet" />

          <div className="base" />

          <div className="base-aux" />
          <div className="wheel-back" />
          <div className="wheel-front" />

          <div className="smoke" />
        </div>
      </div>
    </div>
  );
}