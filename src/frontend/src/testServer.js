/* eslint-disable no-new */
/* eslint-disable import/no-extraneous-dependencies */
import { createServer, Response } from 'miragejs';

const from = [
  { cityUkr: 'Дніпро', cityEng: 'Дніпро', country: 'UA' },
  { cityUkr: 'Київ', cityEng: 'Київ', country: 'UA' },
  { cityUkr: 'Одеса', cityEng: 'Одеса', country: 'UA' },
];
const destination = [
  { cityUkr: 'Дніпро', cityEng: 'Дніпро', country: 'UA' },
  { cityUkr: 'Дністер', cityEng: 'Київ', country: 'UA' },
  { cityUkr: 'Одеса', cityEng: 'Одеса', country: 'UA' },
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
    this.post('/login', () => new Response(200, { Authorization: process.env.REACT_APP_TEST_JWT_TOKEN }, { Authorization: process.env.REACT_APP_TEST_JWT_TOKEN }));
    this.post('/register', () => new Response(200));
    this.post('/confirm-email', () => new Response(200));
    this.post('/reset', () => new Response(200));
    this.post('/new-password', () => new Response(200));
    this.post('/resend-confirm-token', () => new Response(200));
    this.post('/logout', () => new Response(200));
    this.post('/get1', () => new Response(200));
    this.post('/typeAhead', (schema, request) => {
      if (JSON.parse(request.requestBody).startLetters === 'Дн') {
        return new Response(200, undefined, JSON.stringify(from));
      }
      return new Response(200, undefined, JSON.stringify(destination));
    });
    this.post('/searchtickets', () => new Response(200, undefined, JSON.stringify(tickets)) /* { timing: 3000 } */);
    this.post('/sortedby', () => new Response(200, undefined, JSON.stringify(ticketsNew)) /* { timing: 3000 } */);
    this.post('/get', () => {
      document.cookie = 'remember_me=true; path=/; max-age=600';
      return new Response(200, { Authorization: 'alkshfksadfjs2143234' });
    });
  },
});
