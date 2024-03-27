/* eslint-disable import/no-extraneous-dependencies */
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation } from 'react-router-dom';
import Cookies from 'universal-cookie';
import makeQuerry from '../helper/querry';

export default function LoginBtn({ status, updateAuthValue, setIsProfilePopup }) {
  const { t } = useTranslation('translation', { keyPrefix: 'header' });
  const cookies = new Cookies(null, { path: '/' });
  const [logout, setLogout] = useState(false);
  const { pathname } = useLocation();

  function handleLogoutButton() {
    setLogout(false);

    makeQuerry('logout').then((response) => {
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
      <button
        data-testid="logout-btn"
        className="login"
        type="button"
        onClick={() => { setIsProfilePopup(true); }}
      >
        {t('profile')}
      </button>
    );
  }
  return (
    <Link
      data-testid="login-btn"
      className="login-link"
      to="/login"
      state={{ navigate: pathname }}
    >
      {t('login')}
    </Link>
  );
}
