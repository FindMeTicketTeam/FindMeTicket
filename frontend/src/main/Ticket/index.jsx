import React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import './ticket.scss';
import scheduleIcon from './schedule.svg';
import {
  busIcon, trainIcon, everythingIcon,
} from '../Transport/transport-img/img';

export default function Ticket({ data }) {
  const placeFrom = data.placeFrom.length > 25 ? `${data.placeFrom.slice(0, 20)}...` : data.placeFrom;
  const placeAt = data.placeAt.length > 20 ? `${data.placeAt.slice(0, 20)}...` : data.placeAt;
  const placeFromTitle = data.placeFrom.length > 25 ? data.placeFrom : null;
  const placeAtTitle = data.placeAt.length > 20 ? data.placeAt : null;
  const { t } = useTranslation('translation', { keyPrefix: 'ticket' });
  const getIcon = (type) => {
    switch (type) {
      case 'BUS':
        return busIcon;
      case 'TRAIN':
        return trainIcon;
      default:
        return everythingIcon;
    }
  };

  return (
    <div data-testid="ticket" className="ticket">
      <div className="ticket__body">
        <div className="type">
          <div className="type__img"><img src={getIcon(data.type)} alt={data.type} /></div>
        </div>
        <div className="information">
          <div className="information__row">
            <p className="ticket__date">{`${data.departureTime} | ${data.departureDate}`}</p>
            <p className="ticket__travel-time">
              <img src={scheduleIcon} alt="clock" />
              {data.travelTime}
            </p>
            <p className="ticket__date ticket-last-element">{`${data.arrivalTime} | ${data.arrivalDate}`}</p>
          </div>
          <div className="information__row">
            <p className="ticket__city">{data.departureCity}</p>
            <div className="ticket__line" />
            <p className="ticket__city ticket-last-element">{data.arrivalCity}</p>
          </div>
          <div className="information__row">
            <div className="ticket__station" data-title={placeFromTitle}>{placeFrom}</div>
            <div className="ticket__station ticket-last-element" data-title={placeAtTitle}>{placeAt}</div>
          </div>
        </div>
        <div className="price-options">
          <div className="price-options__row">
            <div className="ticket__price">
              <p className="price__ordinry">{`${data.priceMin ? 'Від ' : ''}${Number(data.price ?? data.priceMin).toFixed(2)} ${t('uan')}`}</p>
            </div>
            <Link className="ticket__buy button" to={`/ticket-page/${data.id}`}>{t('select')}</Link>
          </div>
          <p className="ticket__carrier">
            Перевізник:
            {' '}
            {data.carrier}
          </p>
        </div>
      </div>
    </div>
  );
}
