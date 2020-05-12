(ns task-wikipedia-endpoint.routes.revised-functions
  (:require
   [failjure.core :as f]
   [clj-http.client :as client]
   [clojure.data.json :as json]
   [clojure.string :as string]
   [task-wikipedia-endpoint.db.core :as db]))


(def API "https://en.wikipedia.org/api/rest_v1/page/summary/")
(def keywords ["novel" "book" "textbook literature"])

(defn get-book-summary
  "Returns summary if the book title is valid otherwise return \"No Result Found\" using string book-title
   This function is used to get the book summary if book-title is valid"
  [book-title]
  book-title)