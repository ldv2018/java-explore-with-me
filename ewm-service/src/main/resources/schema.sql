DROP TABLE IF EXISTS
    locations,
    users,
    categories,
    "events",
    compilations,
    events_to_compilations,
    participation_request
CASCADE;

CREATE TABLE IF NOT EXISTS locations (
    location_id     serial, --идентификатор локации
    lat             float CHECK (-90 < lat AND lat < 90), --широта отрицательное - ЮП, положительное - СП
    lon             float CHECK (-180 < lon AND lon < 180), --долгота отрицательное - ЗП, положительное - ВП
    CONSTRAINT pk_locations PRIMARY KEY (location_id)
);

CREATE TABLE IF NOT EXISTS users (
    user_id         serial, --идентификатор пользователя
    name            text, --имя пользователя
    email           text, --email пользователя
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT email_uniq UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
    category_id     serial, --идентификатор категории
    name            varchar(25), --имя категории
    CONSTRAINT pk_categories PRIMARY KEY (category_id)
);

CREATE TABLE IF NOT EXISTS "events" (
    event_id            serial, --идентификатор
    category_id         int, --категория fk to categories
    confirmed_request   int, --количество одобренных заявок
    created             timestamp without time zone DEFAULT current_timestamp , --дата и время создания заявки
    description         varchar(7000), --полное описание
    annotation          varchar(2000), --краткое описание
    event_date          timestamp without time zone, --дата и время проведения события
    initiator           int, --создатель fk to users
    location            int, --место проведения fk to locations
    paid                boolean DEFAULT false, --платное участие
    participant_limit   int DEFAULT 0, --ограничение по количеству участников
    published_on        timestamp without time zone, --время публикации
    request_moderation  boolean DEFAULT true, --пре-модерация заявок
    state               varchar(20) DEFAULT 'PENDING', --состояние
    title               varchar(120), --заголовок
    views               int DEFAULT 0, --просмотры
    CONSTRAINT pk_events PRIMARY KEY (event_id),
    CONSTRAINT fk_events_category_id FOREIGN KEY (category_id) REFERENCES categories (category_id),
    CONSTRAINT fk_events_initiator FOREIGN KEY (initiator) REFERENCES users (user_id),
    CONSTRAINT fk_events_location FOREIGN KEY (location) REFERENCES locations (location_id)
);

CREATE TABLE IF NOT EXISTS compilations (
    compilation_id  serial, --идентификатор
    pinned          boolean, --запись закреплена
    title           varchar(100), --заголовок
    CONSTRAINT pk_compilations PRIMARY KEY (compilation_id)
);

CREATE TABLE IF NOT EXISTS events_to_compilations (
    event_id        int, --идентификатор события fk to events
    compilation_id  int, --идентификатор подборки fk to compilations
    CONSTRAINT un_ev_comp UNIQUE (event_id, compilation_id),
    CONSTRAINT fk_events_to_compilations_event_id FOREIGN KEY (event_id) REFERENCES "events" (event_id),
    CONSTRAINT fk_events_to_compilations_compilation_id FOREIGN KEY (compilation_id) REFERENCES compilations (compilation_id)
);

CREATE TABLE IF NOT EXISTS participation_request (
    participation_request_id    serial, --идентификатор заявки
    created                     timestamp without time zone DEFAULT current_timestamp, --время и дата создания заявки
    event_id                    int, --идентификатор события fk to events
    requester                   int, --идентификатор пользователя fk to users
    status                      varchar(20), --статус
    CONSTRAINT pk_participation_request PRIMARY KEY (participation_request_id),
    CONSTRAINT fk_participation_request_event_id FOREIGN KEY (event_id) REFERENCES "events" (event_id),
    CONSTRAINT fk_participation_request_requester FOREIGN KEY (requester) REFERENCES users (user_id)
);

CREATE OR REPLACE FUNCTION calc_confirmed_request()
   RETURNS TRIGGER
AS $confirmed_request_trigger$
DECLARE
	event_id_t int;
request_count_t int;
BEGIN

  IF (TG_OP = 'DELETE') THEN
    event_id_t = OLD.event_id;
ELSE
    event_id_t = NEW.event_id;
END IF;

SELECT COUNT(*)
FROM participation_request
WHERE event_id=event_id_t AND status = 'CONFIRMED'
    INTO request_count_t;

UPDATE "events"
SET confirmed_request = request_count_t
WHERE event_id = event_id_t;
RETURN NEW;
END;
$confirmed_request_trigger$ LANGUAGE plpgsql;

CREATE TRIGGER confirmed_request_trigger
    AFTER INSERT OR UPDATE OR DELETE ON participation_request
    FOR EACH ROW
    EXECUTE PROCEDURE calc_confirmed_request();

