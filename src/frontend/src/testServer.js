/* eslint-disable arrow-body-style */
/* eslint-disable max-len */
/* eslint-disable no-new */
/* eslint-disable import/no-extraneous-dependencies */
import { createServer, Response } from 'miragejs';

// const from = {
//   cities: {
//     Дніпро: 'Ua',
//     Київ: 'Ua',
//     Одеса: 'Ua',
//   },
// };
const destination = [
  {
    cityUa: 'Дніпро', cityEng: 'Dnipro', country: 'UA', siteLanguage: 'eng',
  },
  {
    cityUa: 'Дністеріваіваіва', cityEng: 'Kyivasdadsasd', country: 'UA', siteLanguage: 'eng',
  },
  {
    cityUa: 'Одеса', cityEng: 'Odesa', country: 'UA', siteLanguage: 'eng',
  },
];
const tickets = [
  {
    id: 1,
    departureTime: '8:40',
    departureDate: '29.11, вт',
    travelTime: '8 год 5 хв',
    arrivalTime: '16:30',
    arrivalDate: '29.11, вт',
    departureCity: 'Кам’янець-Подільський',
    arrivalCity: 'Київ',
    placeFrom: 'Зупинка "ТРЦ Магеллан" (кафе KFC), метро Теремки, проспект Академіка Глушкова; будинок 13Б',
    placeAt: 'Зупинка "ТРЦ Магеллан" (кафе KFC), метро Теремки, проспект Академіка Глушкова; будинок 13Б',
    price: '4000',
    tickerCarrier: 'nikkaBus',
    url: 'https://google.com',
  },
  {
    id: 2,
    departureTime: '7:40',
    departureDate: '29.11, вт',
    travelTime: '5 год 45 хв',
    arrivalTime: '16:30',
    arrivalDate: '29.11, вт',
    departureCity: 'Дніпро',
    arrivalCity: 'Івано-Франківськ',
    placeFrom: 'Автовокзал "Центральний"',
    placeAt: 'Южный автовокзал',
    price: '500',
    tickerCarrier: 'nikkaBus',
    url: 'https://google.com',
  },
  {
    id: 3,
    departureTime: '6:40',
    departureDate: '30.11, вт',
    travelTime: '9 год 5 хв',
    arrivalTime: '15:30',
    arrivalDate: '29.11, вт',
    departureCity: 'Одеса',
    arrivalCity: 'Київ',
    placeFrom: 'Автовокзал "Центральний"',
    placeAt: 'Южный автовокзал',
    price: '1000',
    tickerCarrier: 'nikkaBus',
    url: 'https://google.com',
  },
];
const ticketsNew = [
  {
    id: 4,
    departureTime: '8:40',
    departureDate: '29.11, вт',
    travelTime: '8 год 5 хв',
    arrivalTime: '16:30',
    arrivalDate: '29.11, вт',
    departureCity: 'Кам’янець-Подільський',
    arrivalCity: 'Київ',
    placeFrom: 'Автовокзал "Центральний"',
    placeAt: 'Южный автовокзал',
    price: '4000',
    tickerCarrier: 'nikkaBus',
    url: 'https://google.com',
  },
  {
    id: 5,
    departureTime: '7:40',
    departureDate: '29.11, вт',
    travelTime: '5 год 45 хв',
    arrivalTime: '16:30',
    arrivalDate: '29.11, вт',
    departureCity: 'Дніпро',
    arrivalCity: 'Івано-Франківськ',
    placeFrom: 'Автовокзал "Центральний"',
    placeAt: 'Южный автовокзал',
    price: '500',
    tickerCarrier: 'nikkaBus',
    url: 'https://google.com',
  },
  {
    id: 6,
    departureTime: '6:40',
    departureDate: '30.11, вт',
    travelTime: '9 год 5 хв',
    arrivalTime: '15:30',
    arrivalDate: '29.11, вт',
    departureCity: 'Одеса',
    arrivalCity: 'Київ',
    placeFrom: 'Автовокзал "Центральний"',
    placeAt: 'Южный автовокзал',
    price: '1000',
    tickerCarrier: 'nikkaBus',
    url: 'https://google.com',
  },
];
createServer({
  routes() {
    // Responding to a POST request
    this.post('/login', () => {
      // document.cookie = 'rememberMe=cookie-content-here; path=/; expires=123123123123;';
      return new Response(200, { rememberMe: process.env.REACT_APP_TEST_JWT_TOKEN, userId: 1231231421 }, JSON.stringify({ username: 'value', basicPicture: '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' }));
    });
    this.post('/register', () => new Response(200));
    this.post('/oauth2/authorize/google', () => new Response(200, undefined, JSON.stringify({ username: 'Max' })));
    this.post('/confirm-email', () => new Response(200));
    this.post('/reset', () => new Response(200));
    this.post('/new-password', () => new Response(200));
    this.post('/resend-confirm-token', () => new Response(200));
    this.post('/logout', () => new Response(200));
    this.post('/get1', () => new Response(200));
    this.post('/typeAhead', (schema, request) => {
      if (JSON.parse(request.requestBody) === 'Дн') {
        return new Response(200, undefined, JSON.stringify(destination));
      }
      return new Response(200, undefined, JSON.stringify(destination));
    });
    this.post('/searchTickets', () => new Response(200, undefined, JSON.stringify(tickets)) /* { timing: 3000 } */);
    this.post('/sortedBy', () => new Response(200, undefined, JSON.stringify(ticketsNew)), { timing: 1000 });
    this.post('/get', () => {
      document.cookie = 'remember_me=true; path=/; max-age=600';
      return new Response(200, { Authorization: 'alkshfksadfjs2143234' });
    });
  },
});
