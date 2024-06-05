/* eslint-disable import/no-extraneous-dependencies */
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation } from 'react-router-dom';
import Cookies from 'universal-cookie';
import makeQuerry from '../helper/querry';

export default function LoginBtn({ status, updateAuthValue }) {
  const { t } = useTranslation('translation', { keyPrefix: 'header' });
  const cookies = new Cookies(null, { path: '/' });
  const [logout, setLogout] = useState(false);
  const { pathname } = useLocation();

  const successRedirect = ['/tourist-places', '/ticket-page', '/privacy-policy'];

  function handleLogoutButton() {
    setLogout(false);

    makeQuerry('/sign-out').then((response) => {
      switch (response.status) {
        case 200:
          updateAuthValue(null);
          localStorage.removeItem('JWTtoken');
          cookies.remove('rememberMe');
          cookies.remove('USER_ID');
          break;
        default:
          break;
      }
    });
  }

  useEffect(() => {
    if (logout) {
      handleLogoutButton();
    }
  }, [logout]);

  if (status) {
    return (
      <Link
        data-testid="login-btn"
        className="login-link"
        to="/profile-page"
      >
        {t('profile')}
      </Link>
    );
  }
  return (
    <Link
      data-testid="login-btn"
      className="login-link"
      to="/login"
      state={{ successNavigate: successRedirect.includes(pathname) ? pathname : '/', closeNavigate: pathname }}
    >
      {t('login')}
    </Link>
  );
}
