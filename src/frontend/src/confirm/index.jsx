import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { codeCheck } from '../helper/regExCheck';
import Input from '../utils/Input';
import Button from '../utils/Button';
import makeQuerry from '../helper/querry';
import timeOut from '../helper/timer';
import './confirm.css';

export default function Confirm({ changePopup }) {
  const { t } = useTranslation('translation', { keyPrefix: 'confirm' });
  const [code, onCodeChange] = useState('');
  const [codeError, onCodeError] = useState(false);
  const [error, onError] = useState('');
  const [succes, onSucces] = useState(false);
  const [minutes, setMinutes] = useState(1);
  const [seconds, setSeconds] = useState(30);
  const [send, onSend] = useState(false);
  const [resend, onResend] = useState(false);
  const MY_CONSTANT = 'Пошту підтверджено!!!';
  useEffect(() => {
    if (minutes > 0 || seconds > 0) {
      timeOut(seconds, minutes).then((time) => {
        setSeconds(time.seconds);
        setMinutes(time.minutes);
      });
    }
  }, [seconds, minutes]);
  function statusChecks(response) {
    if (response.status === 200) {
      onSucces(true);
      onError('');
    } else if (response.status === 400) {
      onError(t('error-code'));
    } else {
      onError(t('error-server2'));
    }
  }

  function validation() {
    if (codeCheck(code)) {
      onSend(false);
      onError(t('confirm-error'));
      onCodeError(true);
      return false;
    }
    return true;
  }

  function handleSendButton() {
    onCodeError(false);
    if (!validation()) {
      onSend(false);
      return;
    }
    const body = {
      email: sessionStorage.getItem('email'),
      token: code,
    };
    makeQuerry('confirm-email', JSON.stringify(body))
      .then((response) => {
        onSend(false);
        statusChecks(response);
      });
  }

  function statusChecksForResend(response) {
    if (response.status === 200) {
      setMinutes(1);
      setSeconds(30);
    } if (response.status === 419) {
      onError('Спробуйте ще раз');
    } else {
      onError('Помилка серверу. Спробуйте ще раз');
    }
  }

  function handleResendButton() {
    const body = { email: sessionStorage.getItem('email') };
    makeQuerry('resend-confirm-token', JSON.stringify(body))
      .then((response) => {
        onResend(false);
        statusChecksForResend(response);
      });
  }

  function handleCodeChange(value) {
    onCodeChange(value);
    onCodeError(false);
  }
  useEffect(() => {
    if (send) {
      handleSendButton();
    }
  }, [send]);
  useEffect(() => {
    if (resend) {
      handleResendButton();
    }
  }, [resend]);
  return (
    <div data-testid="confirm" className="confirm">
      <div className="form-body">
        <h1 className="title">{t('confirm-email')}</h1>
        {succes && (
        <div className="confirm__success">
          {MY_CONSTANT}
          {' '}
          <p>
            <Link className="link-success" data-testid="" to="/" onClick={() => changePopup(true)}>Натисніть для того щоб авторизуватися</Link>
          </p>
        </div>
        )}
        <p>{t('confirm-code')}</p>
        <p className="confirm__text"><b>{t('confirm-ten')}</b></p>
        <Input error={codeError} dataTestId="confirm-input" value={code} onInputChange={(value) => handleCodeChange(value)} type="text" />
        {error !== '' && <p data-testid="error" className="confirm__error">{error}</p>}
        <div className="row">
          <Button name={send ? 'Обробка...' : t('send')} disabled={send} onButton={onSend} dataTestId="confirm-btn" />
          <button className="confirm__send-again" disabled={minutes > 0 || seconds > 0} onClick={onResend} type="button">{resend ? 'Обробка...' : t('time', { minutes, seconds })}</button>
        </div>
      </div>
    </div>
  );
}
