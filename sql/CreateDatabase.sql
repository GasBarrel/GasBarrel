-- Don't forget to run CreateDatabase.sql from BotCommands

DROP TABLE IF EXISTS gasbarrel_version;

CREATE TABLE gasbarrel_version
(
    one_row BOOL PRIMARY KEY DEFAULT true CHECK (one_row),
    version TEXT NOT null
);

INSERT INTO gasbarrel_version
VALUES (true, '1.0'); -- Change in DatabaseSource.kt too
