/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable import/no-unresolved */
import React, {
  useState, useRef, useCallback, useEffect,
} from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';
import {
  EffectCoverflow, Autoplay, Keyboard, Mousewheel,
} from 'swiper/modules';
import { v4 as uuidv4 } from 'uuid';
import { useTranslation } from 'react-i18next';
import makeQuery from '../helper/querry';

import ReviewsCard from './ReviewsCard';
import ReviewsForm from './ReviewsForm';
import CarouselArrow from './carouselArrow';

import 'swiper/scss';
import './reviews.scss';

// import StepanPhoto from './image 13.png';
// import MykhailoPhoto from './image15.png';
// import MaximPhoto from './image14.png';
// import KirillPhoto from './image16.png';
import loadingIcon from './spinning-loading.svg';

export default function Reviews({ status }) {
  const [reviews, setReviews] = useState([]);

  async function getReviews() {
    const reviewsData = await makeQuery('/reviews', undefined, undefined, 'GET');
    if (reviewsData.status !== 200) {
      return;
    }
    setReviews(reviewsData.body ?? []);
  }

  const { t } = useTranslation('translation', { keyPrefix: 'reviews' });
  const sliderRef = useRef();

  const handlePrev = useCallback(() => {
    if (!sliderRef.current) return;
    sliderRef.current.swiper.slidePrev();
  }, []);

  const handleNext = useCallback(() => {
    if (!sliderRef.current) return;
    sliderRef.current.swiper.slideNext();
  }, []);

  const renderCount = useRef(0);

  useEffect(() => {
    if (renderCount.current === 2) {
      sliderRef.current.swiper.slideTo(reviews.length);
    } else {
      renderCount.current += 1;
    }
  }, [reviews.length]);

  useEffect(() => {
    getReviews();
  }, []);

  return (
    <div className="reviews main">
      <div className="container">
        <h1 className="reviews-title">{t('title')}</h1>
        { reviews.length > 0 ? (
          <div className="reviews-swiper">
            <CarouselArrow direction="left" onClick={handlePrev} />
            <Swiper
              ref={sliderRef}
              effect="coverflow"
              spaceBetween={50}
              slidesPerView={3}
              centeredSlides
              modules={[EffectCoverflow, Autoplay, Keyboard, Mousewheel]}
              coverflowEffect={{
                rotate: 0,
                depth: 150,
                stretch: 0,
                modifier: 1,
                slideShadows: false,
              }}
              autoplay={{
                delay: 3000,
                stopOnLastSlide: true,
              }}
              speed={800}
              keyboard={{
                enabled: true,
              }}
              mousewheel={{
                enabled: true,
              }}
            >
              {reviews.map((slide, index) => (
                <SwiperSlide key={uuidv4()} virtualIndex={index}>
                  <ReviewsCard {...slide} />
                </SwiperSlide>
              ))}
            </Swiper>
            <CarouselArrow direction="right" onClick={handleNext} />
          </div>
        )
          : <img className="reviews-loader" src={loadingIcon} alt="loading..." />}
        <ReviewsForm status={status} setReviews={setReviews} />
      </div>
    </div>
  );
}
