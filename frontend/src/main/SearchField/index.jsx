/* eslint-disable no-shadow */
/* eslint-disable no-unused-vars */
/* eslint-disable import/no-extraneous-dependencies */
/* eslint-disable no-restricted-syntax */
/* eslint-disable react/jsx-no-bind */
import React, {
  useState, useEffect,
} from 'react';
import AsyncSelect from 'react-select/async';
import { useTranslation } from 'react-i18next';
import {
  useSearchParams, Link, useNavigate, useLocation,
} from 'react-router-dom';
import Button from '../../utils/Button';
import Calendar from '../Calendar';
import arrowsImg from './arrows.svg';
import eventSourceQuery2 from '../../helper/eventSourceQuery2';
import makeQuerry from '../../helper/querry';
import useGetCities from '../../hook/useGetCities';
import './searchField.scss';

const dateFormat = new Intl.DateTimeFormat('ru', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
});

export default function SearchField({
  setLoading, setTicketsData, setError, loading, urlSearch, setUrlSearch,
}) {
  const [searchParams] = useSearchParams();
  const { t } = useTranslation('translation', { keyPrefix: 'search' });
  const [cityFrom, setCityFrom] = useState('');
  const [cityTo, setCityTo] = useState('');
  const [errorCityFrom, onErrorCityFrom] = useState(false);
  const [errorCityTo, onErrorCityTo] = useState(false);
  const [date, onDate] = useState(new Date());
  const getCities = useGetCities();
  const noOptionsMessage = (target) => (target.inputValue.length > 1 ? (t('error')) : null);
  const navigate = useNavigate();
  const location = useLocation();

  function validation() {
    if (!cityFrom && !cityTo) {
      onErrorCityFrom(true);
      onErrorCityTo(true);
      return false;
    }
    if (!cityFrom) {
      onErrorCityFrom(true);
      return false;
    }
    if (!cityTo) {
      onErrorCityTo(true);
      return false;
    }
    return true;
  }

  function sendRequestEvents(body) {
    const requestBody = {
      ...body,
      departureDate: dateFormat.format(body.departureDate),
    };
    setLoading(true);
    setTicketsData([]);
    function handleOpen(res) {
      switch (res.status) {
        case 200:
          console.log('open successfully');
          break;
        case 404:
          setError(t('ticket-not-found'));
          break;
        default:
          setError(true);
          break;
      }
    }

    function handleMessage(event) {
      const parsedData = JSON.parse(event.data);
      setTicketsData((prevTickets) => [...prevTickets, parsedData]);
    }

    function handleError() {
      setLoading(false);
    }

    function handleClose() {
      setLoading(false);
    }

    eventSourceQuery2({
      address: '/ticket/search',
      body: JSON.stringify(requestBody),
      handleOpen,
      handleMessage,
      handleError,
      handleClose,
      method: 'POST',
    });
  }

  function sendRequestHTTP(body) {
    const requestBody = {
      ...body,
      departureDate: dateFormat.format(body.departureDate),
    };
    makeQuerry('/tickets/transport', JSON.stringify(requestBody))
      .then((response) => {
        setTicketsData(response.body);
      })
      .catch((error) => {
        console.error('Error fetching data:', error);
      });
  }

  async function sendSortRequest(body) {
    const requestBody = {
      ...body,
      departureDate: dateFormat.format(body.departureDate),
      sortingBy: searchParams.get('sort'),
      ascending: searchParams.get('ascending') === 'true',
    };
    const response = await makeQuerry('/tickets/sort', JSON.stringify(requestBody));

    const responseBody = response.status === 200 ? response.body : null;
    setTicketsData(responseBody);
  }

  function handleRequest() {
    if (!validation()) {
      return;
    }
    navigate(`?type=${searchParams.get('type')}&from=${cityFrom.value}&to=${cityTo.value}&departureDate=${+date}&endpoint=1`);
  }

  useEffect(() => {
    setError(null);
    let params = '';
    if (!(/^.*type=(bus|train|all).*$/).test(location.search)) {
      params += '&type=all';
    }
    if (params && location.pathname !== '/login') {
      navigate(`?${location.search.replace('?', '')}${params}`, { replace: true });
    }
    const body = {
      departureCity: searchParams.get('from'),
      arrivalCity: searchParams.get('to'),
      departureDate: searchParams.get('departureDate') ? new Date(+searchParams.get('departureDate')) : new Date(),
      bus: searchParams.get('type') === 'bus' || searchParams.get('type') === 'all',
      train: searchParams.get('type') === 'train' || searchParams.get('type') === 'all',
      ferry: false,
      airplane: false,
    };
    const endpoint = searchParams.get('endpoint');
    onDate(body.departureDate);
    setCityFrom(body.departureCity ? { value: body.departureCity, label: body.departureCity } : '');
    setCityTo(body.arrivalCity ? { value: body.arrivalCity, label: body.arrivalCity } : '');
    if (urlSearch === location.search) {
      return;
    }
    setUrlSearch(location.search);
    if (searchParams.size < 3 || !body.departureCity || !body.arrivalCity || !endpoint) {
      setTicketsData([]);
      return;
    }
    if (endpoint === '2') {
      sendRequestHTTP(body);
      return;
    }
    if (endpoint === '3') {
      sendSortRequest(body);
      return;
    }
    sendRequestEvents(body);
  }, [searchParams, location]);

  function changeCities() {
    const cityToTemp = cityTo;
    setCityTo(cityFrom);
    setCityFrom(cityToTemp);
  }

  return (
    <div className="search-field">
      <div className={`field ${errorCityFrom ? 'error-select' : ''}`}>
        <div className="field__name">{t('where')}</div>
        <AsyncSelect
          aria-label="cityFromSelect"
          isClearable
          value={cityFrom}
          noOptionsMessage={noOptionsMessage}
          loadingMessage={() => t('loading')}
          cacheOptions
          classNamePrefix="react-select"
          loadOptions={(inputValue) => getCities(inputValue, setCityFrom)}
          placeholder={t('from-placeholder')}
          onChange={setCityFrom}
        />
        {errorCityFrom && <p data-testid="errorCityFrom" className="search-field__error">{t('error2')}</p>}
      </div>
      <button
        className="search-field__img"
        type="button"
        onClick={changeCities}
      >
        <img src={arrowsImg} alt="arrows" />
      </button>
      <div className={`field ${errorCityTo ? 'error-select' : ''}`}>
        <div className="field__name">{t('where-t')}</div>
        <AsyncSelect
          isClearable
          aria-label="cityToSelect"
          value={cityTo}
          noOptionsMessage={noOptionsMessage}
          loadingMessage={() => t('loading')}
          cacheOptions
          classNamePrefix="react-select"
          loadOptions={(inputValue) => getCities(inputValue, setCityTo)}
          placeholder={t('to-placeholder')}
          onChange={setCityTo}
        />
        {errorCityTo && <p data-testid="errorCityTo" className="search-field__error">{t('error2')}</p>}
      </div>
      <Calendar date={date} onDate={onDate} />
      <Button name={t('find')} onButton={handleRequest} disabled={loading} />
    </div>
  );
}
