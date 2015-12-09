/* Test queries */
SELECT 
    *
FROM
    anime;
SELECT 
    *
FROM
    studio;
SELECT 
    *
FROM
    studio_map;
SELECT 
    *
FROM
    characters;
SELECT 
    *
FROM
    character_map;
SELECT 
    *
FROM
    voice_actor;
SELECT 
    *
FROM
    voice_map;
SELECT 
    *
FROM
    episode;

DELIMITER $$
/* Insert a new anime into the database */
CREATE PROCEDURE insert_anime(IN anime_id INT, IN name VARCHAR(200), IN episodes INT, IN start_date DATE, IN end_date DATE, IN score INT, IN rating ENUM('PG','PG-13','R', 'R+'),
IN type ENUM('Movie','TV','Special','OVA'), IN airing VARCHAR(64), IN descr VARCHAR(2000), IN img VARCHAR(500))
BEGIN
INSERT IGNORE INTO anime VALUES (anime_id, name, episodes, start_date, end_date, score, rating, type, airing, descr, img);
END$$

/* Modify anime in the database */
CREATE PROCEDURE modify_anime(IN anime_id INT, IN new_name VARCHAR(200), IN new_episodes INT, IN new_start_date DATE, IN new_end_date DATE, IN new_score INT, IN new_rating ENUM('PG','PG-13','R', 'R+'),
IN new_type ENUM('Movie','TV','Special','OVA'), IN new_airing VARCHAR(64), IN new_descr VARCHAR(2000))
BEGIN
UPDATE anime SET name=new_name, episodes=new_episodes, start_date=new_start_date, end_date=new_end_date, score=new_score, rating=new_rating, type=new_type, airing=new_airing, descr=new_descr
WHERE anime.anime_id=anime_id;
END$$

/* Insert a new voice actor into the database */
CREATE PROCEDURE insert_actor(IN actor_id INT, IN name VARCHAR(200), IN language VARCHAR(200), IN char_id INT)
BEGIN
INSERT IGNORE INTO voice_actor VALUES(actor_id, name, language);
INSERT IGNORE INTO voice_map VALUES(actor_id, char_id);
END$$

/* Insert a new studio into the database */
CREATE PROCEDURE insert_studio(IN studio_id INT, IN name VARCHAR(200), IN anime_id INT)
BEGIN
INSERT IGNORE INTO studio VALUES(studio_id, name);
INSERT IGNORE INTO studio_map VALUES(anime_id, studio_id);
END$$

/* Insert a new episode into the database */
CREATE PROCEDURE insert_episode(IN name VARCHAR(200), IN anime_id INT, IN episode_num INT, IN airdate DATE)
BEGIN
INSERT IGNORE INTO episode VALUES(name, anime_id, episode_num, airdate);
END$$

/* Insert a new character into the database */
CREATE PROCEDURE insert_character(IN char_id INT, IN name VARCHAR(200), IN anime_id INT, IN role ENUM('Main', 'Supporting'), IN img VARCHAR(500))
BEGIN
INSERT IGNORE INTO characters VALUES (char_id, name, role, img);
INSERT IGNORE INTO character_map VALUES(anime_id, char_id);
END$$

/* Get episodes for a specific anime */
CREATE PROCEDURE anime_eps(IN anime_name VARCHAR(200))
BEGIN
SELECT * FROM episode WHERE anime_id = anime_name;
END$$

/* Get characters for a specific anime */
CREATE PROCEDURE chars_in_anime(IN anime_name VARCHAR(200))
BEGIN
SELECT * FROM characters WHERE characters.anime_id = anime_id;
END$$

/* Gets anime data given the name of an anime */
CREATE PROCEDURE show_anime(IN anime_name VARCHAR(200))
BEGIN
SELECT * FROM anime WHERE anime.name = anime_name;
END$$

/* Dynamic means of retrieving show data */
CREATE PROCEDURE filters(IN rating VARCHAR(64), IN type VARCHAR(64), IN score INT, IN airing VARCHAR(64))
BEGIN
SET @base="";
    
IF rating != "all" THEN
SET @base = CONCAT(@base, "anime.rating LIKE \"",rating,"\"");
END IF;
    
    IF type = "all" THEN
SET @base = @base;
ELSE
SET @base = CONCAT(@base, " AND anime.type LIKE \"", type, "\"");
END IF;
    
    IF score>0 THEN
SET @base = CONCAT(@base, " AND anime.score > ", score);
ELSE
SET @base = @base;
END IF;
    
    IF airing = "all" THEN
SET @base = @base;
ELSE
SET @base = CONCAT(@base, " AND anime.airing LIKE \"", airing, "\"");
END IF;
    
    IF @base = "" THEN
SET @base = "SELECT * FROM anime";
ELSE
SET @base = CONCAT("SELECT * FROM anime WHERE ", @base);
END IF;
    
    PREPARE stmt FROM @base;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END$$
CALL filters("all","all",0,"all")$$
DELIMITER ;