/* eslint-disable arrow-body-style */
/* eslint-disable max-len */
/* eslint-disable no-new */
/* eslint-disable import/no-extraneous-dependencies */
import {createServer, Response} from 'miragejs';

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

const reviews = [
    {
        grade: 5,
        writingDate: '12-01-2002',
        reviewText: "I recently had the opportunity to use Findmeticket to look for tickets, and I must say, it exceeded my expectations in several ways. As a frequent traveler, I'm always on the lookout for convenient search propositions and efficient ways to plan my journeys, and Findmeticket certainly delivered on both fronts.",
        username: 'Mykhailo',
        urlPicture: './image15.png',
    },
    {
        grade: 5,
        reviewText: 'НещодавноявикориставзастосунокFindMeTicket,можу сказати,щоцедійснозручний і корисний інструмент для тих, хто подорожує. Сайт пропонує широкий вибір квитків на автобуси та потяги, що дозволяє легко порівнювати ціни та вибирати найбільш вигідні пропозиції.',
        username: 'Степан',
        useravatar: './image 13.png',
    },
    {
        grade: 5,
        reviewText: 'Цей агрегатор - справжнє відкриття! Швидкий пошук, зручний інтерфейс, широкий вибір квитків. Рекомендую усім шукачам найзручніших способів подорожувати!',
        username: 'Максим',
        useravatar: './image14.png',
    },
    {
        grade: 5,
        reviewText: 'Ефективний і зручний агрегатор квитків. Заощаджує час і гроші. Ідеальний для планування подорожей. Відтепер - мій основний інструмент для планування подорожей!',
        username: 'Кирило',
        useravatar: './image16.png',
    },
];

const userReview = {
    reviewText: 'sdfdfsdflksdflsfsfkslfsl',
    grade: 4,
};

createServer({
    routes() {
        // Responding to a POST request
        this.post('/sign-in', () => {
            // document.cookie = 'rememberMe=cookie-content-here; path=/; expires=123123123123;';
            return new Response(200, {
                rememberMe: process.env.REACT_APP_TEST_JWT_TOKEN,
                userId: 1231231421
            }, JSON.stringify({username: 'Max'}));
        });
        this.post('/sign-up', () => new Response(200));
        this.post('/sign-in/google', () => new Response(200, undefined, JSON.stringify({username: 'Max'})));
        this.post('/verify', () => new Response(200));
        this.post('/send/reset-code', () => new Response(200));
        this.patch('/users/password/reset', () => new Response(200));
        this.post('/resend/verification-code', () => new Response(200));
        this.get('/sign-out', () => new Response(200));
        this.post('/get1', () => new Response(200));
        this.post('/tickets/transport', () => new Response(200, undefined, JSON.stringify([])));
        this.post('/tickets/search', () => new Response(200, undefined, JSON.stringify([])));
        this.delete('/users', () => new Response(200));
        this.post('/typeahead', (schema, request) => {
            if (JSON.parse(request.requestBody) === 'Дн') {
                return new Response(200, undefined, JSON.stringify(destination));
            }
            return new Response(200, undefined, JSON.stringify(destination));
        });
        this.post('/tickets/search', () => new Response(200, undefined, JSON.stringify(tickets)) /* { timing: 3000 } */);
        this.post('/tickets/sort', () => new Response(200, undefined, JSON.stringify(ticketsNew)), {timing: 1000});
        this.post('/get', () => {
            document.cookie = 'remember_me=true; path=/; max-age=600';
            return new Response(200, {Authorization: 'alkshfksadfjs2143234'});
        });
        this.get('/users/history', () => new Response(200, undefined, JSON.stringify([
            {
                addingTime: '12.03.2077',
                departureCity: 'Dnipro',
                arrivalCity: 'Kiev',
                bus: true,
                departureDate: '12.23.1990',
            },
            {
                addingTime: '12.03.2022',
                departureCity: 'Dnipro',
                arrivalCity: 'Kiev',
                train: true,
                departureDate: '12.09.2024',
            },
        ])));
        this.get('/reviews', () => new Response(200, undefined, reviews));
        this.get('/users/reviews', () => new Response(200, undefined, userReview));
        this.post('/reviews', () => new Response(200));
        this.post('/updateReview', () => new Response(200));
    },
});
