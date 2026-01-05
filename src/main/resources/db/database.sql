-- --------------------------------------------------
-- Database: charity_event_finder
-- --------------------------------------------------

-- Drop the DB if needed
-- DROP DATABASE IF EXISTS charity_event_finder;
-- CREATE DATABASE charity_event_finder;
-- USE charity_event_finder;

-- --------------------------------------------------
-- TABLES
-- --------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    location VARCHAR(150),
    date DATE NOT NULL,
    time TIME NOT NULL,
    image_url VARCHAR(255),
    registered_count INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(100) NOT NULL,
    event_id INT NOT NULL,
    FOREIGN KEY (user_email) REFERENCES users(email),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    user_email VARCHAR(100) NOT NULL,
    old_username VARCHAR(50),
    new_username VARCHAR(50),
    old_phone VARCHAR(15),
    new_phone VARCHAR(15),
    old_password VARCHAR(255),
    new_password VARCHAR(255),
    event_id INT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- --------------------------------------------------
-- FUNCTIONS
-- --------------------------------------------------

DELIMITER $$

CREATE FUNCTION get_event_count(user_email VARCHAR(100))
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE event_count INT DEFAULT 0;
    SELECT COUNT(*) INTO event_count FROM registrations WHERE registrations.user_email = user_email;
    RETURN event_count;
END $$

CREATE FUNCTION get_total_counts()
RETURNS VARCHAR(255)
DETERMINISTIC
BEGIN
    DECLARE event_count INT DEFAULT 0;
    DECLARE place_count INT DEFAULT 0;
    DECLARE user_count INT DEFAULT 0;

    SELECT COUNT(*), COUNT(DISTINCT location) INTO event_count, place_count FROM events;
    SELECT COUNT(*) INTO user_count FROM users;

    RETURN CONCAT(event_count, ',', place_count, ',', user_count);
END $$

-- --------------------------------------------------
-- TRIGGERS
-- --------------------------------------------------

CREATE TRIGGER after_user_update
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action, user_email, old_username, new_username, old_phone, new_phone, old_password, new_password)
    VALUES ('Updated Profile', NEW.email, OLD.username, NEW.username, OLD.phone, NEW.phone, OLD.password, NEW.password);
END $$

CREATE TRIGGER after_registration_insert
AFTER INSERT ON registrations
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action, user_email, event_id)
    VALUES ('Registered for Event', NEW.user_email, NEW.event_id);
END $$

CREATE TRIGGER after_registration_delete
AFTER DELETE ON registrations
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action, user_email, event_id)
    VALUES ('Deleted Registration', OLD.user_email, OLD.event_id);
END $$

-- --------------------------------------------------
-- EVENT SCHEDULER
-- --------------------------------------------------

SET GLOBAL event_scheduler = ON;

CREATE EVENT IF NOT EXISTS delete_past_events
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    DELETE FROM events WHERE date < CURDATE();
END $$

DELIMITER ;

-- --------------------------------------------------
-- Done!
-- --------------------------------------------------
