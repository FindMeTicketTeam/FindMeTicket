import React from 'react';

function PlaceItem({
  name, img, openNow, rating,
}) {
  const imgUrl = img.getUrl();
  return (
    <div className="place__item">
      <div className="place__info">
        <h3 className="place__name">{name}</h3>
        <div className="place__addInfo">
          <div className="place__rating">
            <img src="" alt="star" />
            <span>{rating}</span>
          </div>
          <p className="place__status">{openNow ? 'Відкрито' : 'Закрито'}</p>
        </div>
      </div>
      <img src={imgUrl} alt={name} className="place__img" />
    </div>
  );
}

export default function PlacePreviewList({ placesInfo }) {
  return (
    <div className="placeList">
      {placesInfo.map((placeInfo) => (
        <PlaceItem
          name={placeInfo.place}
          img={placeInfo.photos[0]}
          openNow={placeInfo.opening_hours.open_now}
          rating={placeInfo.rating}
        />
      ))}
    </div>
  );
}