INSERT INTO restaurant (id, name, address, opening_hour, closing_hour)
VALUES
    ('11111111-1111-4111-8111-111111111111',
     'Roma Trattoria', 'Via Roma 10, Rome', '11:00', '23:00'),

    ('22222222-2222-4222-8222-222222222222',
     'La Notte', 'Via Veneto 20, Rome', '18:00', '02:00'),

    ('33333333-3333-4333-8333-333333333333',
     'Cafe Centrale', 'Piazza Centrale 5, Florence', '09:00', '21:00'),

    ('44444444-4444-4444-8444-444444444444',
     'Osteria Verde', 'Via Verde 8, Florence', '12:00', '00:00');

INSERT INTO trip (id, destination, start_date, end_date, username)
VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
     'Rome', '2027-05-12', '2027-05-17', 'alice'),

    ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
     'Florence', '2027-06-01', '2027-06-07', 'bob'),

    ('cccccccc-cccc-4ccc-8ccc-cccccccccccc',
     'Rome', '2027-07-01', '2027-07-05', 'charlie'),

    ('dddddddd-dddd-4ddd-8ddd-dddddddddddd',
     'Florence', '2027-08-10', '2027-08-14', 'diana');

INSERT INTO trip_restaurant (id, trip_id, restaurant_id, visit_date, visit_time)
VALUES
    ('eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee',
     'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
     '11111111-1111-4111-8111-111111111111',
     '2027-05-13', '19:00'),

    ('ffffffff-ffff-4fff-8fff-ffffffffffff',
     'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
     '22222222-2222-4222-8222-222222222222',
     '2027-05-14', '01:00'),

    ('99999999-9999-4999-8999-999999999999',
     'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
     '33333333-3333-4333-8333-333333333333',
     '2027-06-05', '12:30'),

    ('88888888-8888-4888-8888-888888888888',
     'dddddddd-dddd-4ddd-8ddd-dddddddddddd',
     '44444444-4444-4444-8444-444444444444',
     '2027-08-12', '18:30');
