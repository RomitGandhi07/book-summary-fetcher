-- :name create-book-summary! :! :n
-- :doc creates a new book summary record
INSERT INTO books_summary
(book_title,book_summary)
VALUES (:book_title,:book_summary)

-- :name update-user! :! :n
-- :doc updates an existing user record
--UPDATE users
--SET first_name = :first_name, last_name = :last_name, email = :email
--WHERE id = :id

-- :name get-books-summary :? :1
-- :doc retrieves a summary of books
SELECT * FROM books_summary

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
--DELETE FROM users
--WHERE id = :id
