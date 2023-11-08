import React from 'react';

export default function LoginBtn({ status, changePopup }) {
  if (status) {
    return <button className="login" type="button">Profile</button>;
  }
  return (
    <button className="login" onClick={() => { changePopup(true); }} type="button">Login</button>
  );
}