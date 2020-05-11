(ns task-wikipedia-endpoint.routes.functions
  (:require
   [failjure.core :as f]
   [clj-http.client :as client]
   [clojure.data.json :as json]
   [clojure.string :as string]
   [task-wikipedia-endpoint.db.core :as db]))

(defn validate-numbers
  "This function is used to validate 2 numbers
   It will check both the numbers are of integer type of not"
  [first-number second-number]
  (if (and (integer? first-number) (integer? second-number))
    (+ first-number second-number)
    (f/fail "Please Enter Valid Number")))

(defn add-two-numbers
  "Returns the total of 2 numbers using 2 integer numbers.
   This function is used to sum 2 numbers"
  [first-number second-number]
  (f/attempt-all [addition (validate-numbers first-number second-number)]
                 {:status 200 :body {:total addition}}
                 (f/when-failed [e]
                                {:status 400 :body {:total 0}})))

(defn add-two-numbers-demo
  "Returns the total of 2 numbers using 2 integer numbers.
   This function is used to sum 2 numbers"
  [first-number second-number]
  (+ first-number second-number))

(defn take-book-from-shelf
  "Returns string \"hello\"
   This function is used to return the book-name"
  []
  (str "Hello"))

(def search-url "https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")
(def content-url "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=")
(def summary-url "https://en.wikipedia.org/api/rest_v1/page/summary/")
(def book-keywords ["author" "country" "language" "genre" "publisher" "published" "publication" "pages" "novel"])

(defn spaces->underscores
  "Returns the String where spaces are replaces with underscoers
   Expects  that book-title is a string"
  [book-title]
  (string/replace book-title #" " "_"))

(defn search-result-of-URL
  "Returns body of the API response of the URL using wikipedia URL"
  [URL]
  (-> URL
      (client/get)
      (:body)
      (json/read-str)))

(defn page-id-from-wikipedia-result
  "Returns string by using search-results (map) with keys: \"query\" ,\"pages\" 
   This function is used to to get the page id of the wikipedia page
   Currently the vector after pages key has only one entry so it is easy to get that item.
   Suppose in future if it will become more then 8 results then it will be unordered in that case,
   https://en.wikipedia.org/api/rest_v1/page/title/{wikipedia-page-name} will give page information.
   And from that can get page-id using keys: \"item\", \"page_id\" "
  [search-results]
  (first (keys (get-in search-results ["query" "pages"]))))

(defn summary-from-wikipedia-result
  "Returns map {book-name summary} using search-results(map) with keys:\"title\",\"extract\" 
   This function is used to extract the sumary from the wikipedia page."
  [search-results]
  {(get search-results "title") (get search-results "extract")})

(defn content-from-wikipedia-result
  "Returns string using search-results(map) with keys:\"query\", \"page\", \"page-id\" (will get it using page-id-from-wikipedia-result function),
   Afte page id, key \"revisions\" & then vector will be there so first element of the vector and inside that there will be vector so key \"*\" 
   This function is used to extract the content from the wikipedia page."
  [search-results]
  (let [page-id (page-id-from-wikipedia-result search-results)]
    (get (first (get-in search-results ["query" "pages" page-id "revisions"])) "*")))

(defn check-novel-wikipedia-results
  "Returns Returns map {book-name summary} using book-title string
   This function is used to check whether any wikipedia page available with (novel) or not
   If it is available then it will return the map with structure with above mentioned"
  [book-title]
  (let [wikipedia-URL (str search-url book-title "(novel)")
        search-results (search-result-of-URL wikipedia-URL)]
    (if (pos? (count (second search-results)))
      (let [updated-wikipedia-URL (str summary-url (spaces->underscores (first (second search-results))))
            updated-search-results (search-result-of-URL updated-wikipedia-URL)
            summary (summary-from-wikipedia-result updated-search-results)]
        summary))))

(defn get-all-search-results
  "Returns list of available pages using book-title string
   This function is used to return the valid list of pages name which are similar to book-title"
  [book-title]
  (let [wikipedia-URL (str search-url book-title)
        search-results (search-result-of-URL wikipedia-URL)]
    (if (not (zero? (count (second search-results))))
      (second search-results))))

(defn keyword-match
  "Returns the number which indicates number of matching keywords in the content using list of keywords & content"
  [content]
  (reduce (fn [a b]
            (if (string/includes? content b)
              (inc a)
              a))
          0
          book-keywords))

(defn check-search-result
  "Returns true if no of matching keywords are more then 4 
   This function is used to check the how many keywords are present in the content API"
  [page-name]
  (let [wikipedia-URL (str content-url (spaces->underscores page-name))
        search-results (search-result-of-URL wikipedia-URL)
        content (content-from-wikipedia-result search-results)
        matching-keywords (keyword-match content)]
    (>= matching-keywords 7)))

(defn check-all-search-results
  "Returns idex of page from available valid pages list if any page contains more then 4 keywords using valid pages name list"
  [available-pages]
  (let [no-of-pages (count available-pages)]
    (loop [page-no 0]
      (when (< page-no no-of-pages)
        (let [is-book (check-search-result (get available-pages page-no))]
          (if (true? is-book)
            page-no
            (recur (inc page-no))))))))

(defn insert-summary-into-databse
  "This Function is used to insert the summary into the database
  using the {book-title summary} as input and insert that into the books table"
  [summary]
  (let [title (first (keys summary))
        count (db/check-book-exist {:title title})]
    (if (zero? (get count :count))
      (db/insert-book-summary! {:title title :summary (get summary title)}))))
  

(defn insert-and-return-summary
  "Returns Map {:status 200 :body {book-name summary}} using map with keys: \"book-title\", \"summary\"
   This function is used to insert the book-title and summary into the database and returns response of the wikipedia API request"
  [summary]
  (insert-summary-into-databse summary)
  {:status 200 :body summary})

(defn get-book-summary
  "Returns map {book-title summary} using book-title given by user is valid book title
   Returns map {book-title No Result Found} using book-title given by user is not valid book title"
  [book-title]
  (let [summary (check-novel-wikipedia-results book-title)]
    (if (nil? summary)
      (let [available-pages (get-all-search-results book-title)]
        (if (nil? available-pages)
          {:status 200 :body {book-title "No Result Found..."}}
          (let [book-page-no (check-all-search-results available-pages)]
            (if (= book-page-no nil)
              {:status 200 :body {book-title "No Result Found..."}}
              (let [updated-wikipedia-URL (str summary-url (spaces->underscores (get available-pages book-page-no)))
                    search-results (search-result-of-URL updated-wikipedia-URL)
                    summary (summary-from-wikipedia-result search-results)]
                (insert-and-return-summary summary))))))
      (insert-and-return-summary summary))))

(get-book-summary "Beloved")

(comment
  (nil? 1)
  (defn demo
    [page-name]
    (let [wikipedia-URL (str content-url (spaces->underscores page-name))
          search-results (json/read-str (:body (client/get wikipedia-URL)))
          content (content-from-wikipedia-result search-results)]
      content))

  (def content (demo "MS_Dhoni"))
  content
  (+ 1)
  (reduce (fn [a b]
            (println b))
          []
          (range 10))

  (reduce (fn [a b]
            (if (string/includes? content b)
              (inc a)
              a))
          0
          book-keywords))
