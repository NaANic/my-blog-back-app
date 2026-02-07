-- Drop tables if exist
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;

-- Create posts table
CREATE TABLE posts (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       text TEXT NOT NULL,
                       tags VARCHAR(1000),
                       likes_count INT DEFAULT 0 NOT NULL,
                       comments_count INT DEFAULT 0 NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create comments table
CREATE TABLE comments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          post_id BIGINT NOT NULL,
                          text TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_posts_title ON posts(title);
CREATE INDEX idx_comments_post_id ON comments(post_id);

-- Insert sample data
INSERT INTO posts (title, text, tags, likes_count, comments_count) VALUES
                                                                       ('First Post', 'This is the first blog post content', 'java,spring,tutorial', 10, 2),
                                                                       ('Spring Framework Guide', 'Complete guide to Spring Framework 6.1 without Boot', 'spring,framework,guide', 25, 5),
                                                                       ('Java 21 Features', 'Exploring new features in Java 21', 'java,java21,features', 15, 3);

INSERT INTO comments (post_id, text) VALUES
                                         (1, 'Great post! Very helpful.'),
                                         (1, 'Thanks for sharing this.'),
                                         (2, 'This is exactly what I needed!'),
                                         (2, 'Clear and concise explanation.'),
                                         (2, 'Could you add more examples?'),
                                         (2, 'Excellent tutorial!'),
                                         (2, 'Looking forward to more posts.'),
                                         (3, 'Java 21 is amazing!'),
                                         (3, 'Virtual threads are game changer.'),
                                         (3, 'When will you cover pattern matching?');
