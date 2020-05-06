-- :name insert-book-summary! :! :n
-- :doc inserts a new book summary record
INSERT INTO books
(title,summary)
VALUES (:title,:summary)

-- :name get-books-summary :? :*
-- :doc retrieves a summary of books
SELECT * FROM books

-- :name truncate-books-summary :? :*
-- :doc truncate the books table
TRUNCATE books

-- :name check-book-exist :? :1
-- :doc retrieves count of matching no of book-name
SELECT count(*) FROM books WHERE title=:title;
