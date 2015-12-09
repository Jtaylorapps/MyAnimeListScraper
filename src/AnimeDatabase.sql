/* Create database */
CREATE DATABASE IF NOT EXISTS projectStore;
USE projectStore;

/* Create anime table */
CREATE TABLE IF NOT EXISTS anime (
    anime_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    num_episodes INT,
    start_date DATE,
    end_date DATE,
    score INT,
    rating ENUM('PG', 'PG-13', 'R', 'R+'),
    type ENUM('Movie', 'TV', 'Special', 'OVA'),
    airing VARCHAR(64),
    descr VARCHAR(2000),
    img VARCHAR(500),
    PRIMARY KEY (anime_id)
);
/* Index was added because an anime will need to be queried far more frequently by name than ID */
ALTER TABLE anime ADD INDEX anime_name_index (name);

/* Create episodes table */
CREATE TABLE IF NOT EXISTS episode (
    name VARCHAR(200) NOT NULL,
    anime_id INT NOT NULL,
    episode_num INT NOT NULL,
    airdate DATE,
    FOREIGN KEY (anime_id)
        REFERENCES anime (anime_id)
        ON DELETE CASCADE,
    PRIMARY KEY (anime_id , episode_num)
);

/* Create studio table */
CREATE TABLE IF NOT EXISTS studio (
    studio_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    PRIMARY KEY (studio_id)
);

/* Mapping table for studio to anime */
CREATE TABLE IF NOT EXISTS studio_map (
    anime_id INT NOT NULL,
    studio_id INT NOT NULL,
    FOREIGN KEY (anime_id)
        REFERENCES anime (anime_id)
        ON DELETE CASCADE,
    FOREIGN KEY (studio_id)
        REFERENCES studio (studio_id)
        ON DELETE CASCADE,
    PRIMARY KEY (studio_id , anime_id)
);

/* Create character table */
CREATE TABLE IF NOT EXISTS characters (
    char_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    role ENUM('Main', 'Supporting'),
    img VARCHAR(500),
    PRIMARY KEY (char_id)
);

/* Mapping table for characters to anime */
CREATE TABLE IF NOT EXISTS character_map (
    anime_id INT NOT NULL,
    char_id INT NOT NULL,
    FOREIGN KEY (anime_id)
        REFERENCES anime (anime_id)
        ON DELETE CASCADE,
    FOREIGN KEY (char_id)
        REFERENCES characters (char_id)
        ON DELETE CASCADE,
    PRIMARY KEY (anime_id , char_id)
);

/* Create voice actor table */
CREATE TABLE IF NOT EXISTS voice_actor (
    actor_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    language VARCHAR(200),
    PRIMARY KEY (actor_id)
);

/* Mapping table for voice actors to characters */
CREATE TABLE IF NOT EXISTS voice_map (
    actor_id INT NOT NULL,
    char_id INT NOT NULL,
    FOREIGN KEY (actor_id)
        REFERENCES voice_actor (actor_id)
        ON DELETE CASCADE,
    FOREIGN KEY (char_id)
        REFERENCES characters (char_id)
        ON DELETE CASCADE,
    PRIMARY KEY (actor_id , char_id)
);