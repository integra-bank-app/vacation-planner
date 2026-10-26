INSERT INTO accommodation (id, name, address, city, price_per_night)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Hotel Roma Centro', 'Via Roma 10', 'Rome', 120),
    ('22222222-2222-2222-2222-222222222222', 'Paris Stay', 'Rue de Paris 22', 'Paris', 150),
    ('33333333-3333-3333-3333-333333333333', 'Cluj Apartment', 'Strada Memorandumului 5', 'Cluj', 80),
    ('44444444-4444-4444-4444-444444444444', 'Berlin Central Hotel', 'Alexanderplatz 4', 'Berlin', 110);

INSERT INTO trip (id, destination, start_date, end_date, user_id)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Rome', '2026-10-10', '2026-10-15', 'a1111111-1111-1111-1111-111111111111'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Paris', '2026-11-01', '2026-11-05', 'a1111111-1111-1111-1111-111111111111'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Cluj', '2026-12-01', '2026-12-03', 'a2222222-2222-2222-2222-222222222222'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Berlin', '2026-12-10', '2026-12-15', 'a3333333-3333-3333-3333-333333333333');
INSERT INTO trip_accommodation (trip_id, accommodation_id)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', '44444444-4444-4444-4444-444444444444');
