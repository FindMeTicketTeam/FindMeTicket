import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Input from '../utils/Input';
import Button from '../utils/Button';
import makeQuerry from '../helper/querry';
import './reset.css';
import { emailCheck } from '../helper/regExCheck';

export default function Index() {
  const { t } = useTranslation('translation', { keyPrefix: 'reset' });
  const [email, onEmailChange] = useState('');
  const [emailError, onEmailError] = useState(false);
  const [error, onError] = useState('');
  const [send, onSend] = useState(false);
  const navigate = useNavigate();
  function statusChecks(response) {
    if (response.status === 200) {
      navigate('/change-password');
      sessionStorage.setItem('email', email);
    } else if (response.status === 404) {
      onError('Немає акаунту зареєстрованого на цю електронну пошту');
    } else {
      onError(t('error-server2'));
    }
  }

  function validation() {
    if (emailCheck(email)) {
      onError(t('reset-error'));
      onEmailError(true);
      return false;
    }
    return true;
  }

  function handleButton() {
    onEmailError(false);
    if (!validation()) {
      onSend(false);
      return;
    }
    makeQuerry('reset', JSON.stringify(email))
      .then((response) => {
        onSend(false);
        statusChecks(response);
      });
  }

  function handleEmailChange(value) {
    onEmailChange(value);
    onEmailError(false);
  }
  useEffect(() => {
    if (send) {
      handleButton();
    }
  }, [send]);
  return (
    <div className="reset">
      <div className="form-body reset__body">
        <h1 className="title">{t('password-reset')}</h1>
        <p className="reset__text">{t('email')}</p>
        <Input error={emailError} value={email} onInputChange={(value) => handleEmailChange(value)} type="text" placeholder="mail@mail.com" dataTestId="reset-input" />
        {error !== '' && <p data-testid="error" className="reset__error">{error}</p>}
        <Button name={send ? t('processing') : t('send')} disabled={send} className="reset__btn" onButton={onSend} dataTestId="reset-btn" />
      </div>
    </div>
  );
}