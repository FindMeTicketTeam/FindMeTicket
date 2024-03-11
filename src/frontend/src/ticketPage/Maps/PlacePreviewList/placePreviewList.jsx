import React from 'react';
import PlacePreviewItem from '../PlacePreviewItem/placePreviewItem';
import './placePreviewList.scss';

export default function PlacePreviewList({ placesInfo, setCurrentPlaceId, updateMarker }) {
  return (
    <div className="placeList">
      {placesInfo.map((placeInfo) => (
        <PlacePreviewItem
          key={placeInfo.place_id}
          name={placeInfo.name}
          img={placeInfo.photos[0].getUrl()}
          openNow={placeInfo.opening_hours.isOpen}
          rating={placeInfo.rating}
          onClick={() => {
            setCurrentPlaceId(placeInfo.place_id);
            updateMarker(placeInfo);
          }}
        />
      ))}
    </div>
  );
}