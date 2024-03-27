import { fetchEventSource } from '@microsoft/fetch-event-source';

async function eventSourceQuery2({
  address, body, handleMessage, handleError, handleClose, handleOpen, method = 'GET', headers,
}) {
  try {
    await fetchEventSource(`http://localhost:${process.env.REACT_APP_PORT}/${address}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        ...headers,
      },
      body,
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
        if (handleOpen) {
          handleOpen(res);
        }
      },
      onmessage(event) {
        if (handleMessage) {
          handleMessage(event);
        }
        console.log('event', event);
      },
      onclose() {
        console.log('Connection closed by the server');
        if (handleClose) {
          handleClose();
        }
        throw new Error('Connection closed');
      },
      onerror(err) {
        if (handleError) {
          handleError(err);
        }
        throw err;
      },
    });
  } catch (error) {
    console.log('There was an error or connection was closed', error);
  }
}

export default eventSourceQuery2;
