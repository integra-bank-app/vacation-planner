
INSERT INTO flights (
    id,
    flight_number,
    departure_airport_code,
    arrival_airport_code,
    departure_time,
    arrival_time,
    number_of_seats
) VALUES
      ('11111111-1111-4111-8111-111111111111',
       'RO101', 'CLJ', 'CDG',
       '2027-06-10 08:00:00+03',
       '2027-06-10 10:30:00+02', 3),

      ('22222222-2222-4222-8222-222222222222',
       'RO102', 'CDG', 'CLJ',
       '2027-06-15 12:00:00+02',
       '2027-06-15 15:30:00+03', 3),

      ('33333333-3333-4333-8333-333333333333',
       'RO201', 'CLJ', 'FCO',
       '2027-07-01 09:00:00+03',
       '2027-07-01 10:45:00+02', 2),

      ('44444444-4444-4444-8444-444444444444',
       'RO202', 'FCO', 'CLJ',
       '2027-07-05 14:00:00+02',
       '2027-07-05 17:00:00+03', 2);


INSERT INTO trip (
    id,
    destination,
    start_date,
    end_date,
    username
) VALUES
      ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
       'Paris', '2027-06-10', '2027-06-15', 'tudor'),

      ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
       'Rome', '2027-07-01', '2027-07-05', 'tudor'),

      ('cccccccc-cccc-4ccc-8ccc-cccccccccccc',
       'Paris', '2027-06-10', '2027-06-15', 'bianca');


INSERT INTO trip_flight (trip_id, flight_id) VALUES
                                                 ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
                                                  '11111111-1111-4111-8111-111111111111'),

                                                 ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
                                                  '22222222-2222-4222-8222-222222222222'),

                                                 ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
                                                  '33333333-3333-4333-8333-333333333333'),

                                                 ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
                                                  '44444444-4444-4444-8444-444444444444'),

                                                 ('cccccccc-cccc-4ccc-8ccc-cccccccccccc',
                                                  '11111111-1111-4111-8111-111111111111'),

                                                 ('cccccccc-cccc-4ccc-8ccc-cccccccccccc',
                                                  '22222222-2222-4222-8222-222222222222');
