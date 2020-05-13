(ns task-wikipedia-endpoint.routes.revised-functions
  (:require
   [failjure.core :as f]
   [clj-http.client :as client]
   [clojure.data.json :as json]
   [clojure.string :as string]
   [slingshot.slingshot :refer :all])
  (:use clojure.tools.logging))

(def API "https://en.wikipedia.org/api/rest_v1/page/summary/")
(def keywords ["novel" "book" "textbook literature"])

(defn response-of-URL
  "Returns response body of the URL which is map with keys:\"type\" \"description\" \"title\" using string book-title
   This function is used to get body of the response of URl (https://en.wikipedia.org/api/rest_v1/page/summary/{book-title})
   If the book-title is not valid wikipedia page then it will retrn nil"
  [book-title]
  (try+
   (-> API
       (str book-title)
       (client/get)
       (:body)
       (json/read-str))
   (catch [:status 404] {:keys [request-time headers body]}
     (warn "NOT Found 404" request-time headers body))
   (catch Object _
     (error (:throwable &throw-context) "unexpected error")
     (throw+))))


(defn check-novel-wikipedia-page
  "Returns summary which is map {book-name summary} of the wikipedia page using string book-title
   This fuction is used to retrive & return the summary using book-title if it is valid wikipedia page otherwise it will return nil"
  [book-title]
  (let [summary (response-of-URL (str book-title "_(novel)"))]
    (if (not (nil? summary))
      {(get summary "title") (get summary "extract")})))

(defn valid-wikipedia-book-page?
  "Returns true if it is book-page otherwise it will return false using map with keys: \"description\"
   This function is used to check whether the title given by user is book or not"
  [body-response]
  (let [description (string/lower-case (get body-response "description"))
        total-keywords (count keywords)]
    (loop [i 0]
      (when (< i total-keywords)
        (if (string/includes? description (keywords i))
          true
          (recur (inc i)))))))

(defn no-result-found-response
  "Returns map with keys {:status 200 :body {book-tile No result Found}} using string book-title"
  [book-title]
  {:status 200 :body {book-title "No result Found"}})

(defn get-book-summary
  "Returns summary if the book title is valid otherwise return \"No Result Found\" using string book-title
   This function is used to get the book summary if book-title is valid"
  [book-title]
  (let [novel-result (check-novel-wikipedia-page book-title)]
    (if (nil? novel-result)
      (let [response (response-of-URL book-title)
            type (string/lower-case (get response "type"))]
        (if (= type "standard")
          (let [is-book? (valid-wikipedia-book-page? response)]
            (if (true? is-book?)
              {:status 200 :body {(get response "title") (get response "extract")}}
              (no-result-found-response book-title)))
          (no-result-found-response book-title)))
      {:status 200 :body novel-result})))

(comment
    
  (:a {:a 1 :b 2})
  (get {"a" 1 "b" 2} "b")
  ("b" {"a" 5 "b" 2})
  (f/attempt-all [body (response-of-URL "The_Glass_Bead_game")]
                 {:status 200 :body {"Demo" "Yor are Lucky"}}
                 (f/when-failed [e]
                                {:status 200 :body {"Demo" "No result Found"}}))

  (def a (try+
          (-> API
              (str "The_Glass_bead_Game")
              (client/get)
              (:body)
              (json/read-str))
          (catch [:status 403] {:keys [request-time headers body]}
            (warn "403" request-time headers))
          (catch [:status 404] {:keys [request-time headers body]}
            (warn "NOT Found 404" request-time headers body))
          (catch Object _
            (error (:throwable &throw-context) "unexpected error")
            (throw+))))

  (response-of-URL "The_Glass_Bead_Game")

  a
  (nil? a)
  (def a "https://en.wikipedia.org/api/rest_v1/page/summary/The_glass_bead_game")
  (client/get a)
  (-> a
      (client/get))

  (:body (client/get (str API "The_glass_bead_Game") {:throw-entire-message? true})))