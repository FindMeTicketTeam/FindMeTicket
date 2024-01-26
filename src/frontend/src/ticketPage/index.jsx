/* eslint-disable import/no-extraneous-dependencies */
/* eslint-disable no-restricted-syntax */
import React, { useEffect } from 'react';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { useParams } from 'react-router-dom';
// import Price from './Price/index';
// import Information from './Information/index ';
// import Maps from './Maps/index';
// import eventSourceQuery from '../helper/eventSourceQuery';
import './style.css';

function TicketPage() {
  const { ticketId } = useParams();
  // const [ticketData, setTicketData] = useState(null);
  // const [ticketUrl, setTicketUrl] = useState([]);

  // async function serverRequest() {
  //   const dataStream = await eventSourceQuery('digits', undefined, undefined, 'GET');
  //   for await (const chunk of dataStream) {
  //     console.log(chunk);
  //     // if (Object.prototype.hasOwnProperty.call(chunk, 'ticketname')) {
  //     //   setTicketData(chunk);
  //     // } else {
  //     //   setTicketUrl(((prevLinks) => [...prevLinks, chunk]));
  //     // }
  //   }
  // }

  console.log(ticketId);

  async function serverRequest() {
    await fetchEventSource(`http://localhost:/8080/tickets/${ticketId}`, {
      method: 'POST',
      headers: {
        Accept: 'text/event-stream',
      },
      onopen(res) {
        if (res.ok && res.status === 200) {
          console.log('Connection made ', res);
        } else if (
          res.status >= 400
          && res.status < 500
          && res.status !== 429
        ) {
          console.log('Client side error ', res);
        }
      },
      onmessage(event) {
        console.log(event.data);
        const parsedData = JSON.parse(event.data);
        console.log(parsedData);
      },
      onclose() {
        console.log('Connection closed by the server');
      },
      onerror(err) {
        console.log('There was an error from server', err);
      },
    });
  }

  useEffect(() => {
    serverRequest();
  }, []);

  return (
    <div className="ticket-page-container">
      {/* <div className="ticketPage-header">{ticketData.date}</div>
      <Information ticketData={ticketData} />
      <div className="ticketPage-text">Ціни</div>
      <Price ticketUrl={ticketUrl} />
      <Maps /> */}
      <h2>testing</h2>
    </div>
  );
}

export default TicketPage;
