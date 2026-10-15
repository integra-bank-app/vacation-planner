INSERT INTO users (id, username, email, first_name, last_name) VALUES
                                                                   ('a1111111-1111-1111-1111-111111111111', 'ana.pop', 'ana.pop@gmail.com', 'Ana', 'Pop'),
                                                                   ('a2222222-2222-2222-2222-222222222222', 'mihai.ionescu', 'mihai.ionescu@yahoo.com', 'Mihai', 'Ionescu'),
                                                                   ('a3333333-3333-3333-3333-333333333333', 'elena.dumitru', 'elena.dumitru@icloud.com', 'Elena', 'Dumitru'),
                                                                   ('a4444444-4444-4444-4444-444444444444', 'andrei.stan', 'andrei.stan@vacationplanner.com', 'Andrei', 'Stan');

INSERT INTO trip (id, destination, start_date, end_date, user_id) VALUES
                                                                      ('b1111111-1111-1111-1111-111111111111', 'Paris', '2026-07-01', '2026-07-07', 'a1111111-1111-1111-1111-111111111111'),
                                                                      ('b2222222-2222-2222-2222-222222222222', 'Rome', '2026-08-10', '2026-08-15', 'a1111111-1111-1111-1111-111111111111'),
                                                                      ('b3333333-3333-3333-3333-333333333333', 'Barcelona', '2026-09-05', '2026-09-12', 'a2222222-2222-2222-2222-222222222222'),
                                                                      ('b4444444-4444-4444-4444-444444444444', 'Vienna', '2026-12-20', '2026-12-27', 'a3333333-3333-3333-3333-333333333333');
