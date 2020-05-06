-- :name insert-book-summary! :! :n
-- :doc inserts a new book summary record
INSERT INTO books
(title,summary)
VALUES (:title,:summary)

-- :name get-books-summary :? :*
-- :doc retrieves a summary of books
SELECT * FROM books

