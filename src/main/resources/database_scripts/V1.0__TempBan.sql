create table temp_ban
(
    id         serial                   not null primary key,
    guild_id   bigint                   not null,
    user_id    bigint                   not null,
    expires_at timestamp with time zone not null,
    reason     text                     not null check (length(trim(reason)) != 0)
);

CREATE FUNCTION temp_ban_check_no_existing_bans() RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    IF EXISTS (SELECT *
               FROM temp_ban tempBan
               WHERE tempBan.user_id = new.user_id
                 AND tempBan.guild_id = new.guild_id
                 AND tempBan.expires_at > now()) THEN
        RAISE EXCEPTION 'A temp ban already exists for this member';
    END IF;

    RETURN new;
END
$$;

CREATE TRIGGER temp_ban_before_insert_check_existing_bans
    BEFORE INSERT
    ON temp_ban
    FOR EACH ROW
EXECUTE FUNCTION temp_ban_check_no_existing_bans();
