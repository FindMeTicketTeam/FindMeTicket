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
      return new Response(200, { rememberMe: process.env.REACT_APP_TEST_JWT_TOKEN, userId: 1231231421 }, JSON.stringify({ username: 'Max' }));
    });
    this.post('/register', () => new Response(200));
    this.post('/oauth2/authorize/google', () => new Response(200, undefined, JSON.stringify({ username: 'Max' })));
    this.post('/confirm-email', () => new Response(200));
    this.post('/reset', () => new Response(200));
    this.post('/new-password', () => new Response(200));
    this.post('/resend-confirm-token', () => new Response(200));
    this.post('/logout', () => new Response(200));
    this.post('/get1', () => new Response(200));
    this.post('/selectedTransport', () => new Response(200, undefined, JSON.stringify([])));
    this.post('/searchTickets', () => new Response(200, undefined, JSON.stringify([])));
    this.delete('/delete-user', () => new Response(200));
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
