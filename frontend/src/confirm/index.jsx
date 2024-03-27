import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { codeCheck } from '../helper/regExCheck';
import Input from '../utils/Input';
import Button from '../utils/Button';
import makeQuerry from '../helper/querry';
import timeOut from '../helper/timer';
import './confirm.scss';

export default function Confirm() {
  const { t } = useTranslation('translation', { keyPrefix: 'confirm' });
  const [code, setCodeChange] = useState('');
  const [codeError, setCodeError] = useState(false);
  const [error, setError] = useState('');
  const [success, setSucces] = useState(false);
  const [minutes, setMinutes] = useState(1);
  const [seconds, setSeconds] = useState(30);
  const [send, setSend] = useState(false);
  const [resend, setResend] = useState(false);
  const sendButtonIsDisabled = send || success;
  const resendButtonIsDisabled = (minutes > 0 || seconds > 0) || success;

  useEffect(() => {
    if (minutes > 0 || seconds > 0) {
      timeOut(seconds, minutes).then((time) => {
        setSeconds(time.seconds);
        setMinutes(time.minutes);
      });
    }
  }, [seconds, minutes]);

  function statusChecks(response) {
    switch (response.status) {
      case 200:
        setSucces(true);
        setError('');
        break;
      case 400:
        setError(t('error-code'));
        break;
      default:
        setError(t('error-server2'));
        break;
    }
  }

  function validation() {
    if (codeCheck(code)) {
      setSend(false);
      setError(t('error-code'));
      setCodeError(true);
      return false;
    }
    return true;
  }

  function handleSendButton() {
    if (!validation()) {
      setSend(false);
      return;
    }
    const body = {
      email: sessionStorage.getItem('email'),
      token: code,
    };
    makeQuerry('confirm-email', JSON.stringify(body))
      .then((response) => {
        setSend(false);
        statusChecks(response);
      });
  }

  function statusChecksForResend(response) {
    if (response.status === 200) {
      setMinutes(1);
      setSeconds(30);
    } else if (response.status === 419) {
      setError(t('error-server1'));
    } else {
      setError(t('error-server2'));
    }
  }

  function handleResendButton() {
    const body = { email: sessionStorage.getItem('email') };
    makeQuerry('resend-confirm-token', JSON.stringify(body))
      .then((response) => {
        setResend(false);
        statusChecksForResend(response);
      });
  }

  function handleCodeChange(value) {
    setCodeChange(value);
    setCodeError(false);
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
    <div data-testid="confirm" className="confirm main">
      <div className="form-body">
        <h1 className="title">{t('confirm-email')}</h1>
        {success && (
        <div className="confirm__success">
          {t('success-message')}
          {' '}
          <p>
            <Link
              className="link-success"
              data-testid=""
              to="/login"
              state={{ navigate: '/' }}
            >
              {t('auth-link')}
            </Link>

          </p>
        </div>
        )}
        <p>{t('confirm-code')}</p>
        <p className="confirm__text"><b>{t('confirm-ten')}</b></p>
        <Input
          error={codeError}
          dataTestId="confirm-input"
          value={code}
          onInputChange={(value) => handleCodeChange(value)}
          type="text"
        />
        {error !== '' && <p data-testid="error" className="confirm__error">{error}</p>}
        <div className="row">
          <Button
            name={send ? t('processing') : t('send')}
            disabled={sendButtonIsDisabled}
            onButton={setSend}
            dataTestId="confirm-btn"
          />

          <button
            data-testid="send-again-btn"
            className="confirm__send-again"
            disabled={resendButtonIsDisabled}
            onClick={setResend}
            type="button"
          >
            {resend ? t('processing') : t('time', { minutes, seconds })}
          </button>
        </div>
      </div>
    </div>
  );
}
