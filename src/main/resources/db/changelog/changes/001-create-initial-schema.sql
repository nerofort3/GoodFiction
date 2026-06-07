-- liquibase formatted sql

-- changeset neroforte:1
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(255),
    is_profile_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP WITH TIME ZONE
);

-- changeset neroforte:2
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    google_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) UNIQUE,
    thumbnail_url VARCHAR(2000),
    published_date VARCHAR(255),
    description TEXT,
    page_count INTEGER
);

-- changeset neroforte:3
CREATE TABLE book_categories (
    book_id BIGINT NOT NULL,
    category VARCHAR(255) NOT NULL,
    PRIMARY KEY (book_id, category),
    CONSTRAINT fk_book_categories_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

-- changeset neroforte:4
CREATE TABLE user_books (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_status VARCHAR(255) NOT NULL,
    user_rating INTEGER,
    finished_percentage DOUBLE PRECISION,
    review VARCHAR(500),
    CONSTRAINT fk_user_books_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_books_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

-- changeset neroforte:5
CREATE TABLE activity_feed (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    activity_type VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_activity_feed_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_feed_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);
