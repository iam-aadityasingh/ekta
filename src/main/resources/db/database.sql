-- Optional: create and use the database
-- DROP DATABASE IF EXISTS charity_event_finder;
-- CREATE DATABASE charity_event_finder;
-- USE charity_event_finder;

SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

-- =========================
-- TABLES
-- =========================

DROP TABLE IF EXISTS event_audit_log;
CREATE TABLE event_audit_log (
  id                   INT(11) NOT NULL AUTO_INCREMENT,
  action               VARCHAR(30)  NOT NULL,
  event_id             INT(11)      NOT NULL,
  old_eventname        VARCHAR(100) DEFAULT NULL,
  new_eventname        VARCHAR(100) DEFAULT NULL,
  old_description      VARCHAR(255) DEFAULT NULL,
  new_description      VARCHAR(255) DEFAULT NULL,
  old_location         VARCHAR(150) DEFAULT NULL,
  new_location         VARCHAR(150) DEFAULT NULL,
  old_date             DATE         DEFAULT NULL,
  new_date             DATE         DEFAULT NULL,
  old_time             TIME         DEFAULT NULL,
  new_time             TIME         DEFAULT NULL,
  old_image_url        VARCHAR(255) DEFAULT NULL,
  new_image_url        VARCHAR(255) DEFAULT NULL,
  old_registered_count INT(11)      DEFAULT NULL,
  new_registered_count INT(11)      DEFAULT NULL,
  timestamp            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id       INT(11)      NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) DEFAULT NULL,
  password VARCHAR(255) NOT NULL,
  email    VARCHAR(100) NOT NULL,
  phone    VARCHAR(15)  DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_email    (email),
  UNIQUE KEY uq_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS events;
CREATE TABLE events (
  id               INT(11)      NOT NULL AUTO_INCREMENT,
  name             VARCHAR(100) NOT NULL,
  description      TEXT         NOT NULL,
  location         VARCHAR(255) NOT NULL,
  date             DATE         NOT NULL,
  time             TIME         NOT NULL,
  image_url        VARCHAR(255) DEFAULT NULL,
  registered_count INT(11)      DEFAULT 0,
  creator_id       INT(11)      NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_events_name (name),
  KEY idx_events_creator_id (creator_id),
  CONSTRAINT fk_events_creator
    FOREIGN KEY (creator_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS profile_audit_log;
CREATE TABLE profile_audit_log (
  id           INT(11)      NOT NULL AUTO_INCREMENT,
  action       VARCHAR(255) NOT NULL,
  user_email   VARCHAR(100) NOT NULL,
  old_username VARCHAR(255) NOT NULL,
  new_username VARCHAR(255) NOT NULL,
  old_phone    VARCHAR(15)  NOT NULL,
  new_phone    VARCHAR(15)  NOT NULL,
  old_password VARCHAR(255) NOT NULL,
  new_password VARCHAR(255) NOT NULL,
  timestamp    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS registrations;
CREATE TABLE registrations (
  id       INT(11) NOT NULL AUTO_INCREMENT,
  user_id  INT(11) NOT NULL,
  event_id INT(11) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_reg_user_id  (user_id),
  KEY idx_reg_event_id (event_id),
  CONSTRAINT fk_reg_user
    FOREIGN KEY (user_id)  REFERENCES users (id)   ON DELETE CASCADE,
  CONSTRAINT fk_reg_event
    FOREIGN KEY (event_id) REFERENCES events (id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS registrations_audit_log;
CREATE TABLE registrations_audit_log (
  id       INT(11)      NOT NULL AUTO_INCREMENT,
  action   VARCHAR(40)  NOT NULL,
  user_id  INT(11)      NOT NULL,
  event_id INT(11)      NOT NULL,
  timestamp DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- SAMPLE DATA (users, events only)
-- =========================

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO users (id, username, password, email, phone) VALUES
(1,'Aaditya Singh','@16Sep2005','iamaadityasingh16@gmail.com','0000111188'),
(2,'Rudra Singh','abcdef','rudra_singh@gmail.com','6644778855'),
(3,'Krishnaja','bullshit','shee@gmail.com','9420123657');

INSERT INTO events (id, name, description, location, date, time, image_url, registered_count, creator_id) VALUES
(1,'Yuvati Yamuna','We aim to bring bck the youth of Yamuna','Wazirabad barrage, Delhi','2025-10-19','10:00:00','https://myprojects101.blob.core.windows.net/event-images/event1.jpg',0,3),
(2,'Paw Protectors','Animals are family, not property.','Bandra Fort, Mumbai','2026-01-26','10:30:00','https://myprojects101.blob.core.windows.net/event-images/event2.jpg',0,1),
(3,'swachh ganga','maano to main ganga maa hoon, na mano to bahata paani','Prayagraj, Uttar Pradesh','2025-12-01','11:00:00','https://myprojects101.blob.core.windows.net/event-images/event3.jpg',0,2),
(4,'jaan to jaan hai','kyonki janavar jarurat nahi jaruri hai','Louis Vuitton Mumbai 1 Taj Mahal Palace & Tower','2025-12-01','11:00:00','https://myprojects101.blob.core.windows.net/event-images/event4.jpg',0,2),
(5,'Beaches on the can','Let''s pick trash not shells!','Juhu, Mumbai','2025-12-01','11:00:00','https://myprojects101.blob.core.windows.net/event-images/event5.jpg',0,1),
(6,'Whitening Himalaye','Wall of defence, endangered!','Leh-Manali Highway, South end - Manali, Himachal Pradesh','2025-12-01','11:00:00','https://myprojects101.blob.core.windows.net/event-images/event6.jpg',0,3);

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- FUNCTIONS
-- =========================

DELIMITER $$

CREATE FUNCTION get_event_count(p_user_email VARCHAR(100))
RETURNS INT
DETERMINISTIC
BEGIN
  DECLARE v_event_count INT DEFAULT 0;

  SELECT COUNT(*)
  INTO v_event_count
  FROM registrations r
  JOIN users u ON r.user_id = u.id
  WHERE u.email = p_user_email;

  RETURN v_event_count;
END $$

CREATE FUNCTION get_total_counts()
RETURNS VARCHAR(255)
DETERMINISTIC
BEGIN
  DECLARE event_count INT DEFAULT 0;
  DECLARE place_count INT DEFAULT 0;
  DECLARE user_count  INT DEFAULT 0;

  SELECT COUNT(*), COUNT(DISTINCT location)
  INTO event_count, place_count
  FROM events;

  SELECT COUNT(*)
  INTO user_count
  FROM users;

  RETURN CONCAT(event_count, ',', place_count, ',', user_count);
END $$

-- =========================
-- TRIGGERS
-- =========================

CREATE TRIGGER after_user_update
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
  INSERT INTO profile_audit_log (
    action, user_email,
    old_username, new_username,
    old_phone,    new_phone,
    old_password, new_password
  )
  VALUES (
    'Updated Profile', NEW.email,
    OLD.username, NEW.username,
    OLD.phone,    NEW.phone,
    OLD.password, NEW.password
  );
END $$

CREATE TRIGGER after_registration_insert
AFTER INSERT ON registrations
FOR EACH ROW
BEGIN
  INSERT INTO registrations_audit_log (action, user_id, event_id)
  VALUES ('Registered for Event', NEW.user_id, NEW.event_id);
END $$

CREATE TRIGGER after_registration_delete
AFTER DELETE ON registrations
FOR EACH ROW
BEGIN
  INSERT INTO registrations_audit_log (action, user_id, event_id)
  VALUES ('Deleted Registration', OLD.user_id, OLD.event_id);
END $$

-- =========================
-- EVENT SCHEDULER
-- =========================

SET GLOBAL event_scheduler = ON $$

CREATE EVENT IF NOT EXISTS delete_past_events
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
  DELETE FROM events WHERE date < CURDATE();
END $$

DELIMITER ;

SET SQL_MODE = '';
