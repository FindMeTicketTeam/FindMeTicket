import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import Field from '../utils/Field';
import Button from '../utils/Button';
import makeQuerry from '../helper/querry';
import { codeCheck, passwordCheck } from '../helper/regExCheck';
import timeOut from '../helper/timer';

export default function Index() {
  const [code, setCodeChange] = useState('');
  const [codeError, setCodeError] = useState(false);
  const [password, setPasswordChange] = useState('');
  const [passwordError, setPasswordError] = useState(false);
  const [confirmPassword, setConfirmPasswordChange] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState(false);
  const [error, setError] = useState('');
  const [minutes, setMinutes] = useState(1);
  const [seconds, setSeconds] = useState(30);
  const [success, setSucces] = useState(false);
  const [send, setSend] = useState(false);
  const [resend, setResend] = useState(false);
  const [show, setShow] = useState(false);
  const { t } = useTranslation('translation', { keyPrefix: 'reset-password' });
  const sendButtonIsDisabled = send || success;
  const resendButtonIsDisabled = (minutes > 0 || seconds > 0) || success;

  function handleCodeInput(value) {
    setCodeChange(value);
    setCodeError(false);
  }

  function handlePasswordInput(value) {
    setPasswordChange(value);
    setPasswordError(false);
  }

  function handleConfirmPasswordInput(value) {
    setConfirmPasswordChange(value);
    setConfirmPasswordError(false);
  }

  useEffect(() => {
    if (minutes > 0 || seconds > 0) {
      timeOut(seconds, minutes).then((time) => {
        setSeconds(time.seconds);
        setMinutes(time.minutes);
      });
    }
  }, [seconds, minutes]);

  function checkResponseForResend(response) {
    switch (response.status) {
      case 200:
        setMinutes(1);
        setSeconds(30);
        break;
      case 419:
        setError(t('error-server'));
        break;
      default:
        setError(t('error-server2'));
        break;
    }
  }

  function handleResendButton() {
    setError('');
    const body = { email: sessionStorage.getItem('email') };
    makeQuerry('resend-confirm-token', JSON.stringify(body))
      .then((response) => {
        checkResponseForResend(response);
        setResend(false);
      });
  }

  function checkResponse(response) {
    if (response.status === 200) {
      setSucces(true);
    } else if (response.status === 400) {
      setError(t('error-code'));
    } else {
      setError(t('error-server2'));
    }
  }

  function validation() {
    switch (true) {
      case codeCheck(code):
        setError(t('code-error'));
        setCodeError(true);
        return false;
      case passwordCheck(password):
        setError(t('password-error'));
        setPasswordError(true);
        return false;
      case password !== confirmPassword:
        setError(t('confirm-password-error'));
        setConfirmPasswordError(true);
        return false;
      default:
        return true;
    }
  }

  function handleSendButton() {
    setError('');
    if (!validation()) {
      setSend(false);
      return;
    }
    const body = {
      token: code,
      password,
      email: sessionStorage.getItem('email'),
      confirmPassword,
    };
    makeQuerry('new-password', JSON.stringify(body))
      .then((response) => {
        setSend(false);
        checkResponse(response);
      });
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
    <div className="confirm main">
      <div className="form-body">
        <h1 className="title">{t('title')}</h1>
        {success && (
        <p className="confirm__success">
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
        </p>
        )}
        <p className="confirm__text">{t('confirm-text1')}</p>
        <p className="confirm__text"><b>{t('confirm-text2')}</b></p>
        {error !== '' && <p data-testid="error" className="error">{error}</p>}
        <Field
          dataTestId="code-input"
          error={codeError}
          name={t('code-input-title')}
          value={code}
          type="text"
          onInputChange={(value) => handleCodeInput(value)}
        />

        <Field
          dataTestId="password-input"
          error={passwordError}
          name={t('password-input-title')}
          value={password}
          type="password"
          onInputChange={(value) => handlePasswordInput(value)}
          tip={t('password-tip')}
          show={show}
          setShow={setShow}
        />

        <Field
          dataTestId="confirm-password-input"
          error={confirmPasswordError}
          name={t('confirm-password-title')}
          value={confirmPassword}
          type="password"
          onInputChange={(value) => handleConfirmPasswordInput(value)}
          show={show}
          setShow={setShow}
        />
        <Button
          name={send ? t('processing') : t('button-title')}
          className="confirm__btn"
          onButton={setSend}
          disabled={sendButtonIsDisabled}
          dataTestId="change-password-btn"
        />

        <button
          data-testid="confirm-send-btn"
          className="confirm__send-again"
          disabled={resendButtonIsDisabled}
          onClick={setResend}
          type="button"
        >
          {resend ? t('processing') : t('time', { minutes, seconds })}
        </button>
      </div>
    </div>
  );
}
