CREATE DATABASE IF NOT EXISTS movie_rating_db;
USE movie_rating_db;

CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(80) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS movies (
  movie_id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(150) NOT NULL,
  genre VARCHAR(80) NOT NULL,
  release_year INT NOT NULL
);

CREATE TABLE IF NOT EXISTS ratings (
  rating_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  movie_id INT NOT NULL,
  rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  review TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
  UNIQUE (user_id, movie_id)
);

INSERT INTO movies(title, genre, release_year)
SELECT 'Interstellar', 'Sci-Fi', 2014
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Interstellar');
INSERT INTO movies(title, genre, release_year)
SELECT 'The Dark Knight', 'Action', 2008
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='The Dark Knight');
INSERT INTO movies(title, genre, release_year)
SELECT '3 Idiots', 'Comedy', 2009
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='3 Idiots');
INSERT INTO movies(title, genre, release_year)
SELECT 'Inception', 'Sci-Fi', 2010
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Inception');
