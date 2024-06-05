/* eslint-disable max-len */
import React from 'react';
import './style.scss';
import {lockImage, rank1, rank2, rank3, rank4, rank5,} from './img/img';

function AvatarPopup({ closeAvatarPopup, avatar }) {
  return (
    <div className="avatar-popup">
      <div className="avatar-popup-content">
        <div className="image-container">
          <div className="image-card-rank1">
            <img src={rank1} alt="rank1" className="image-rank1" />
            <img src={avatar} alt="Avatar" className="avatar-overlay" />
            <span className="level-text">Lvl 1</span>
          </div>

          <div className="image-card">
            <div className="image-dark-body">
              <img src={rank2} alt="rank2" className="image-rank2" />
              <img src={avatar} alt="Avatar" className="avatar-overlay" />
              <span className="level-text">Lvl 2</span>
            </div>
            <img src={lockImage} alt="lock" className="lock-image" />
          </div>
          <div className="image-card">
            <div className="image-dark-body">
              <img src={rank3} alt="rank3" className="image-rank3" />
              <img src={avatar} alt="Avatar" className="avatar-overlay" />
              <span className="level-text">Lvl 3</span>
            </div>
            <img src={lockImage} alt="lock" className="lock-image" />
          </div>
          <div className="image-card">
            <div className="image-dark-body">
              <img src={rank4} alt="rank4" className="image-rank4" />
              <img src={avatar} alt="Avatar" className="avatar-overlay" />
              <span className="level-text">Lvl 4</span>
            </div>
            <img src={lockImage} alt="lock" className="lock-image" />
          </div>
          <div className="image-card">
            <div className="image-dark-body">
              <img src={rank5} alt="rank5" className="image-rank5" />
              <img src={avatar} alt="Avatar" className="avatar-overlay" />
              <span className="level-text">Lvl 5</span>
            </div>
            <img src={lockImage} alt="lock" className="lock-image" />
          </div>
          {/* <div className="image-card">
            <img src={rank6} alt="rank6" className="image-rank6" />
            <img src={lockImage} alt="lock" className="lock-image" /> */}
          {/* <img src={socialMediaAvatar || defaultAvatar || Ellipse} alt="Avatar" className="avatar-overlay-e" /> */}
          {/* <span className="level-text">Lvl 1</span>
          </div> */}
        </div>
        <button className="close-button-rank" type="button" onClick={closeAvatarPopup}>
          &times;
        </button>
      </div>
    </div>
  );
}

export default AvatarPopup;
