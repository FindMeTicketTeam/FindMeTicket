/* eslint-disable no-useless-concat */
import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useSearchParams, useNavigate, useLocation } from 'react-router-dom';
import InProgress from '../../InProgress/index';
import './transport.scss';
import {
  busIcon, trainIcon, planeIcon, boatIcon, everythingIcon,
} from './transport-img/img';
import loaderAnim from '../loader.svg';

function TransportButton({
  label, isActive, onClick, img, disabled, loading,
}) {
  return (
    <button
      type="button"
      className={`transport-btn ${isActive ? 'active' : ''} ${(disabled || loading) ? 'disabled' : ''}`}
      onClick={onClick}
      disabled={disabled || loading}
    >
      <img
        className="transportation"
        src={img}
        alt={label}
      />
      {(loading && !disabled) ? <img src={loaderAnim} alt="Loading anim" /> : label}
    </button>
  );
}

function Transport({
  setSelectedTransport, loading,
}) {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [isOpen, setIsOpen] = useState(false);
  const { t } = useTranslation('translation', { keyPrefix: 'transport' });

  const handleButtonClick = (button) => {
    navigate(location.search.replace(/bus|all|train/, button).replace(/(endpoint=)\d+/, '$1' + '2'));
    setSelectedTransport(button);
  };

  return (
    <div>
      <TransportButton
        label={t('everything')}
        isActive={searchParams.get('type') === 'all'}
        onClick={() => { handleButtonClick('all'); }}
        img={everythingIcon}
        loading={loading}
      />
      <TransportButton
        label={t('bus')}
        isActive={searchParams.get('type') === 'bus'}
        onClick={() => { handleButtonClick('bus'); }}
        img={busIcon}
        loading={loading}
      />
      <TransportButton
        label={t('train')}
        isActive={searchParams.get('type') === 'train'}
        onClick={() => { handleButtonClick('train'); }}
        img={trainIcon}
        loading={loading}
      />
      <TransportButton
        label={t('plane')}
        onClick={() => setIsOpen(true)}
        img={planeIcon}
        loading={loading}
        disabled
      />
      <TransportButton
        label={t('ferry')}
        onClick={() => setIsOpen(true)}
        img={boatIcon}
        loading={loading}
        disabled
      />
      {isOpen && <InProgress title={t('message-title')} text={t('message-text')} setIsOpen={setIsOpen} />}
    </div>
  );
}

export default Transport;
