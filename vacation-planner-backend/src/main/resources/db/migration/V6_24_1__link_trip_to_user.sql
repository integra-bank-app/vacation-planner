ALTER TABLE trip ADD COLUMN user_id UUID;

UPDATE trip SET user_id = (SELECT id FROM users WHERE users.username = trip.username);

DELETE FROM trip WHERE user_id IS NULL;

ALTER TABLE trip ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE trip ADD CONSTRAINT fk_trip_user_id FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE trip DROP COLUMN username;
