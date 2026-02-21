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
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);

-- Insert sample data (теги в формате #tag1#tag2#)
INSERT INTO posts (title, text, tags, likes_count, comments_count) VALUES
                                                                       ('Getting Started with Spring Framework',
                                                                        'Spring Framework is a powerful framework for building Java applications. In this comprehensive guide, we will explore the core concepts and best practices for developing modern web applications using Spring.',
                                                                        '#java#spring#tutorial#',
                                                                        10, 2),

                                                                       ('Java 21 New Features',
                                                                        'Java 21 introduces several exciting features including virtual threads, pattern matching enhancements, and record patterns. These improvements make Java development more efficient and code more readable. Virtual threads are particularly game-changing for high-concurrency applications.',
                                                                        '#java#java21#features#programming#',
                                                                        25, 5),

                                                                       ('Spring Data JDBC vs JPA',
                                                                        'Understanding the differences between Spring Data JDBC and JPA is crucial for making the right architectural decisions. Spring Data JDBC provides a simpler, more transparent approach without the complexity of JPA.',
                                                                        '#spring#jdbc#jpa#database#',
                                                                        15, 3),

                                                                       ('RESTful API Design Best Practices',
                                                                        'Designing good RESTful APIs requires careful consideration of resource naming, HTTP methods, status codes, and error handling. Follow these best practices to create APIs that are intuitive and maintainable.',
                                                                        '#rest#api#webdev#bestpractices#',
                                                                        30, 8),

                                                                       ('Markdown Tutorial for Developers',
                                                                        'Markdown is a lightweight markup language that is widely used for documentation. Learn how to format text, create lists, add code blocks, and more using simple syntax.',
                                                                        '#markdown#documentation#tutorial#',
                                                                        12, 4);

-- Insert comments
INSERT INTO comments (post_id, text) VALUES
                                         (1, 'Great introduction! Very helpful for beginners.'),
                                         (1, 'Thanks for sharing this comprehensive guide.'),

                                         (2, 'Virtual threads are amazing! Can''t wait to use them in production.'),
                                         (2, 'The performance improvements are significant.'),
                                         (2, 'Great explanation of pattern matching!'),
                                         (2, 'When will Java 21 LTS be released?'),
                                         (2, 'This is the future of Java development.'),

                                         (3, 'I prefer JPA for complex domain models.'),
                                         (3, 'Spring Data JDBC is perfect for microservices.'),
                                         (3, 'Very clear comparison, thank you!'),

                                         (4, 'These practices saved me hours of debugging!'),
                                         (4, 'What about GraphQL vs REST?'),
                                         (4, 'Excellent article on API design.'),
                                         (4, 'The status codes section is particularly useful.'),
                                         (4, 'Should we use HATEOAS?'),
                                         (4, 'Great examples and code snippets.'),
                                         (4, 'I disagree with point 3 about versioning.'),
                                         (4, 'This should be required reading for all backend developers.'),

                                         (5, 'Markdown makes documentation so much easier!'),
                                         (5, 'I use it every day in GitHub.'),
                                         (5, 'Could you add a section about tables?'),
                                         (5, 'Simple and effective tutorial.');
