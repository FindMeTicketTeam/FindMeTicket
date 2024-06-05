/* eslint-disable import/no-extraneous-dependencies */
import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { GoogleLogin } from '@react-oauth/google';
import FacebookLogin from '@greatsumini/react-facebook-login';
import { passwordCheck, emailCheck } from '../../helper/regExCheck';
import Field from '../../utils/Field';
import Button from '../../utils/Button';
import makeQuerry from '../../helper/querry';
import Checkbox from '../../utils/Checkbox';
import facebookIcon from './facebook.png';
import './login.scss';

export default function Popup({ updateAuthValue }) {
  const { t } = useTranslation('translation', { keyPrefix: 'login' });
  const [login, setLoginChange] = useState('');
  const [loginError, setLoginError] = useState(false);
  const [password, setPasswordChange] = useState('');
  const [passwordError, setPasswordError] = useState(false);
  const [error, setError] = useState('');
  const [send, setSend] = useState(false);
  const [remember, rememberMe] = useState(false);
  const [show, onShow] = useState(false);
  const { state } = useLocation();

  function statusChecks(response) {
    switch (response.status) {
      case 200:
        updateAuthValue(response.body);
        break;
      case 401:
        setError(t('error-lp'));
        break;
      default:
        setError(t('error-server2'));
        break;
    }
  }

  function validation() {
    switch (true) {
      case emailCheck(login):
        setError(t('login-error'));
        setLoginError(true);
        return false;
      case passwordCheck(password):
        setError(t('password-error'));
        setPasswordError(true);
        return false;
      default:
        return true;
    }
  }

  function handleClick() {
    if (!validation()) {
      setSend(false);
      return;
    }
    const body = {
      email: login,
      password,
      rememberMe: remember,
    };
    makeQuerry('/sign-in', JSON.stringify(body))
      .then((response) => {
        setSend(false);
        statusChecks(response);
      });
  }

  async function auth2Request(provider, credential) {
    const bodyJSON = JSON.stringify({ idToken: credential });
    const response = await makeQuerry(`/sign-in/${provider}`, bodyJSON);
    switch (response.status) {
      case 200:
        // navigate(-2);
        // window.history.back();
        updateAuthValue(response.body);
        break;
      case 401:
        setError('Помилка. Спробуйте ще раз');
        break;
      default:
        setError(t('error-server2'));
        break;
    }
  }

  useEffect(() => {
    if (send) {
      handleClick();
    }
  }, [send]);

  function handleLoginChange(value) {
    setLoginChange(value);
    setLoginError(false);
    setError('');
  }

  function handlePasswordChange(value) {
    setPasswordChange(value);
    setPasswordError(false);
    setError('');
  }

  function handleRememberMeChange() {
    rememberMe(!remember);
  }

  return (
    <div data-testid="login" className="background main">
      <div className="popup__body">
        <Link to={state.closeNavigate ?? '/'} className="close" aria-label="Close" />
        {error !== '' && <p data-testid="error" className="error">{error}</p>}
        <Field
          error={loginError}
          dataTestId="login-input"
          name={t('email-name')}
          tip={t('login-tip')}
          value={login}
          type="email"
          onInputChange={(value) => handleLoginChange(value)}
          placeholder="mail@mail.com"
        />

        <Field
          error={passwordError}
          dataTestId="password-login-input"
          name={t('password-name')}
          tip={t('password-tip')}
          value={password}
          type="password"
          onInputChange={(value) => handlePasswordChange(value)}
          show={show}
          onShow={onShow}
        />

        <Checkbox id="rememberMe" onChange={() => handleRememberMeChange()}>{t('remember-me')}</Checkbox>
        <div className="link">
          <Link
            to="/reset"
          >
            {t('forgot-password')}
          </Link>
        </div>

        <Button
          dataTestId="login-btn"
          className="btn-full"
          disabled={send}
          name={send ? t('processing') : t('login-buttom')}
          onButton={setSend}
        />

        <div className="login__another">
          <span className="login-another__line" />
          <span className="login-another__content">{t('or')}</span>
          <span className="login-another__line" />
        </div>
        <GoogleLogin
          onSuccess={(credentialResponse) => {
            setError('');
            auth2Request('google', credentialResponse.credential);
          }}
          onError={() => {
            setError('Помилка. Спробуйте ще раз');
          }}
          shape="circle"
          width={336}
        />
        <FacebookLogin
          appId="927706882244929"
          className="login__google"
          onSuccess={(response) => {
            setError('');
            auth2Request('facebook', response.userID);
          }}
          onFail={() => {
            setError('Помилка. Спробуйте ще раз');
          }}
          onProfileSuccess={() => {
          }}
        >
          <img src={facebookIcon} alt="logo" />
          {t('facebook')}

        </FacebookLogin>
        <div className="link link-register">
          <Link
            data-testid="to-register-btn"
            to="/register"
          >
            {t('register')}
          </Link>
        </div>

      </div>
    </div>
  );
}
