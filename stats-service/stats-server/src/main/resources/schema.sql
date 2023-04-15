DROP TABLE IF EXISTS
    hits
CASCADE;

CREATE TABLE IF NOT EXISTS hits (
    hit_id      serial, --идентификатор
    app         varchar(25), --наименование сервиса
    uri         varchar(50),    --uri для которого был запрос
    ip          varchar(15),    --ip адрес с которого был запрос
    req_time    timestamp without time zone, --время запроса
    CONSTRAINT pk_hits PRIMARY KEY (hit_id)
);