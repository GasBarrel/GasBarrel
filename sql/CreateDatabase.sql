-- Don't forget to run CreateDatabase.sql from BotCommands

DROP TABLE IF EXISTS gasbarrel_version;

CREATE TABLE gasbarrel_version
(
    one_row BOOL PRIMARY KEY DEFAULT TRUE CHECK (one_row),
    version TEXT NOT NULL
);

INSERT INTO gasbarrel_version
VALUES (true, '1.0'); -- Change in DatabaseSource.kt too
