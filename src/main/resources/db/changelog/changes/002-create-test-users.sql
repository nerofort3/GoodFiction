-- liquibase formatted sql

-- changeset neroforte:7
INSERT INTO users (username, email, password, roles, is_profile_public, created_date)
VALUES
    ('Neroforte', 'wwlddss@gmail.com', '$2a$12$7MOVM2mt9U6XpGe7fWXT8.Ai/au.x0FhwBPvXCRocq79et0DIJNYG', 'ROLE_USER, ROLE_ADMIN', true, NOW()),
    ('UserTest1', 'usrtest@gmail.com', '$2a$12$bQrBDzWClGCdQ4NAGQt0h.oVdltKrE/V0nx/kC/lkIwVEOLytv08u', 'ROLE_USER', false, NOW()),
    ('HellOfAReader', 'hellofareader@gmail.com', '$2a$12$VRimE5fEG1rXPfNRC7DeKe2gQdIdUfGvPlCh2zY1ykRsWyaSKOhw.', 'ROLE_USER', true, NOW())
ON CONFLICT DO NOTHING;