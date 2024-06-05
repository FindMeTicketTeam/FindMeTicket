/* eslint-disable max-len */
import React, { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import eventSourceQuery2 from '../helper/eventSourceQuery2';
import Price from './Price/index';
import Information from './Information/index ';
import Loader from '../Loader/index';
import PriceTrain from './PriceTrain/index';
import Error from '../Error';
import Maps from './Maps';
import './style.scss';

function TicketPage() {
  const { ticketId } = useParams();
  const [ticketData, setTicketData] = useState(null);
  const [ticketUrl, setTicketUrl] = useState(new Set());
  const [ticketError, setTicketError] = useState(false);
  const [connection, setConnection] = useState(true);
  const { t } = useTranslation('translation', { keyPrefix: 'ticket-page' });

  async function serverRequest() {
    function handleOpen(res) {
      switch (res.status) {
        case 200:
          console.log('open successfully');
          break;
        default:
          setTicketError(true);
          break;
      }
    }

    function handleMessage(event) {
      const parsedData = JSON.parse(event.data);
      if (event.event === 'ticket info') {
        setTicketData(parsedData);
        return;
      }
      setTicketUrl((prevTicketUrl) => [...prevTicketUrl, {
        resource: event.event, url: parsedData.url, price: parsedData.price, comfort: parsedData.comfort,
      }]);
    }

    function handleError() {
      if (!ticketData) {
        console.log('ticket data in error message:', ticketData);
      }
    }

    function handleClose() {
      setConnection(false);
    }
    eventSourceQuery2({
      address: `/tickets/${ticketId}`,
      handleMessage,
      handleError,
      handleOpen,
      handleClose,
    });
  }

  const handleServerRequest = useCallback(() => serverRequest(), []);

  useEffect(() => {
    handleServerRequest();
  }, []);

  const PriceView = ticketData?.type === 'TRAIN' ? <PriceTrain ticketUrls={ticketUrl} connection={connection} /> : <Price ticketUrls={ticketUrl} connection={connection} />;
  const mapView = ticketData?.placeAt ? <Maps address={`${ticketData.placeAt},${ticketData.arrivalCity}`} /> : <Error />;

  const ticketDataView = ticketData && (
    <>
      <div className="ticketPage-header">{`${ticketData.departureDate} - ${ticketData.arrivalDate}`}</div>
      <Information ticketData={ticketData} />
      <div className="ticketPage-text">{t('price')}</div>
      {PriceView}
      {mapView}
    </>
  );
  const ticketErrorView = ticketError && <Error />;
  const ticketLoadingView = (!ticketError && !ticketData) && <Loader />;

  return (
    <div className="ticket-page-container">
      {ticketDataView}
      {ticketErrorView}
      {ticketLoadingView}
    </div>
  );
}

export default TicketPage;
