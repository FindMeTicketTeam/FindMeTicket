import React from 'react';
import './style.scss';
import PriceBlock from './PriceBlock';
import spinningLoaderIcon from '../spinning-loading.svg';

function Price({ ticketUrls, connection }) {
  return (
    <div className="ticket-price">
      {ticketUrls.length > 0
      && ticketUrls.map((ticketUrl) => <PriceBlock ticketUrl={ticketUrl} />)}
      {connection && <img className="ticket-price__loading" src={spinningLoaderIcon} alt="loader" />}
      {(ticketUrls === 0 && !connection) && <p>Error...</p>}
    </div>
  );
}

export default Price;
