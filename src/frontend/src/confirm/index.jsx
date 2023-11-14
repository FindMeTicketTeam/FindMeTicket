import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { codeCheck } from '../helper/regExCheck';
import Input from '../components/utlis/Input';
import Button from '../components/utlis/Button';
import makeQuerry from '../helper/querry';
import './confirm.css';

export default function Confirm({ changePopup }) {
  const { t } = useTranslation('translation', { keyPrefix: 'confirm' });
  const [code, onCodeChange] = useState('');
  const [codeError, onCodeError] = useState(false);
  const [error, onError] = useState('');
  const [succes, onSucces] = useState(false);
  const [minutes, setMinutes] = useState(1);
  const [seconds, setSeconds] = useState(30);
  useEffect(() => {
    if (minutes > 0 || seconds > 0) {
      setTimeout(() => {
        if (seconds === 0) {
          setMinutes(minutes - 1);
          setSeconds(59);
        } else {
          setSeconds(seconds - 1);
        }
      }, 1000);
    }
  }, [seconds, minutes]);
  function sendCode(body) {
    makeQuerry('confirm-email', JSON.stringify(body))
      .then((response) => {
        if (response.status === 200) {
          onSucces(true);
          onError('');
        } else if (response.status === 400) {
          onError('Код не правильний');
        } else if (response.status === 404) {
          onError("З'єднання з сервером відсутнє");
        } else {
          onError('Помилка серверу. Спробуйте ще раз');
        }
      });
  }
  function handleSendButton() {
    onCodeError(false);
    if
    (codeCheck(code)) {
      onError(t('confirm-error'));
      onCodeError(true);
      return;
    }
    const body = {
      email: sessionStorage.getItem('email'),
      code,
    };
    sendCode(body);
  }
  function handleResendButton() {
    const body = { email: sessionStorage.getItem('email') };
    makeQuerry('resend-confirm-token', JSON.stringify(body))
      .then((response) => {
        if (response.status === 200) {
          setMinutes(2);
        } else if (response.status === 404) {
          onError("З'єднання з сервером відсутнє");
        } else {
          onError('Помилка серверу. Спробуйте ще раз');
        }
      });
  }
  return (
    <div data-testid="confirm" className="confirm">
      <h1 className="title">{t('confirm-email')}</h1>
      {succes && (
      <p className="confirm__success">
        Пошту підтвержено!!!
        {' '}
        <p>
          <Link className="link-success" data-testid="" to="/" onClick={() => changePopup(true)}>Натисніть для того щоб авторизуватися</Link>
        </p>
      </p>
      )}
      <p className="confirm__text">{t('confirm-code')}</p>
      <p className="confirm__text"><b>{t('confirm-ten')}</b></p>
      <Input error={codeError} dataTestId="confirm-input" value={code} onInputChange={onCodeChange} type="text" />
      {error !== '' && <p className="confirm__error">{error}</p>}
      <Button name={t('send')} className="confirm__btn" onButton={() => handleSendButton()} />
      <button className="confirm__send-again" disabled={minutes > 0 || seconds > 0} onClick={() => handleResendButton()} type="button">{t('time', { minutes, seconds })}</button>
    </div>
  );
}
