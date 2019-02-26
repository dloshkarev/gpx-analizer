--ACTIVITY
CREATE SEQUENCE IF NOT EXISTS activity_seq;
CREATE TABLE activity
(
    id bigint PRIMARY KEY DEFAULT NEXTVAL('activity_seq'),
    request_guid text NOT NULL DEFAULT md5(random()::text),
    event_date timestamp NOT NULL,
    distance double precision NOT NULL,
    duration double precision NOT NULL,
    elevation_gain double precision NOT NULL,
    elevation_loss double precision NOT NULL,
    avg_hr double precision,
    avg_pace smallint NOT NULL,
    running_index double precision NOT NULL
);