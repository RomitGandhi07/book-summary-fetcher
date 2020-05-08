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
(def book-keywords ["author","country","language","genre","publisher","published","publication","pages","novel"])

(defn spaces->underscores
  "Returns the String where spaces are replaces with underscoers
   Expects  that book-title is a string"
  [book-title]
  (string/replace book-title #" " "_"))

(defn page-id-from-wikipedia-result
  "Returns string by using search-results (map) with keys: \"query\" ,\"pages\" 
   This function is used to to get the page id of the wikipedia page"
  [search-results]
  (nth (keys (get-in search-results ["query" "pages"])) 0))

(defn summary-from-Wikipedia-result
  "Returns map {book-name summary} using search-results(map) with keys:\"title\",\"extract\" 
   This function is used to extract the sumary from the wikipedia page."
  [search-results]
  {(get search-results "title") (get search-results "extract")})

(defn content-from-Wikipedia-result
  "Returns string using search-results(map) with keys:\"query\",\"page\",page-id,\"revisiions\",0,\"*\" 
   This function is used to extract the content from the wikipedia page."
  [search-results]
  (let [page-id (page-id-from-wikipedia-result search-results)]
  (get-in search-results ["query" "pages" page-id "revisions" 0 "*"])))

(defn check-novel-wikipedia-results
  "Returns Returns map {book-name summary} using book-title string
   This function is used to check whether any wikipedia page available with (novel) or not
   If it is available then it will return the map with structure with above mentioned"
  [book-title]
  (let [wikipedia-URL (str search-url book-title "(novel)")
        search-results (json/read-str (:body (client/get wikipedia-URL)))]
    (if (>= (count (get search-results 1)) 1)
      (let [updated-wikipedia-URL (str summary-url (spaces->underscores (get (get search-results 1) 0)))
            updated-search-results (json/read-str (:body (client/get updated-wikipedia-URL)))
            summary (summary-from-Wikipedia-result updated-search-results)]
        summary))))

(defn get-all-search-results
  "Returns list of available pages using book-title string
   This function is used to return the valid list of pages name which are similar to book-title"
  [book-title]
  (let [wikipedia-URL (str search-url book-title)
        search-results (json/read-str (:body (client/get wikipedia-URL)))]
    (if (not= 0 (count (get search-results 1)))
      (get search-results 1))))

(defn keyword-match
  "Returns the number which indicates number of matching keywords in the content using list of keywords & content" 
  [keywords content]
  (if (empty? keywords)
    0
    (let [keyword (nth keywords 0)]
      (if (string/includes? content keyword)
          (+ 1 (keyword-match (rest keywords) content))
          (recur (rest keywords) content)))))

(defn check-search-result
  "Returns true if no of matching keywords are more then 4 
   This function is used to check the how many keywords are present in the content API"
  [page-name]
  (let [wikipedia-URL (str content-url (spaces->underscores page-name))
        search-results (json/read-str (:body (client/get wikipedia-URL)))
        content (content-from-Wikipedia-result search-results)
        matching-keywords (keyword-match book-keywords content)]
    (if (>= matching-keywords 6)
      true)))

(defn check-all-search-results
  "Returns idex of page from available valid pages list if any page contains more then 4 keywords using valid pages name list"
  [available-pages]
  (let [no-of-pages (count available-pages)]
    (loop [page-no 0]
      (when (< page-no no-of-pages)
        (let [is-book (check-search-result (get available-pages page-no))]
          (if (= is-book true)
            page-no
            (recur (inc page-no))))))))
    
(defn insert-summary-into-databse
  "This Function is used to insert the summary into the database
  using the {book-title summary} as input and insert that into the books table"
  [summary]
  (let [title (nth (keys summary) 0)
        count (db/check-book-exist {:title title})]
    (if (= (get count :count) 0)
      (db/insert-book-summary! {:title title :summary (get summary title)}))))

(defn get-book-summary
  "Returns map {book-title summary} using book-title given by user is valid book title
   Returns map {book-title No Result Found} using book-title given by user is not valid book title"
  [book-title]
  (let [summary (check-novel-wikipedia-results book-title)]
    (if (= nil summary)
      (do
        (let [available-pages (get-all-search-results book-title)]
          (if (= nil available-pages)
            {:status 200 :body {book-title "No Result Found..."}}  
              (let [book-page-no (check-all-search-results available-pages)]
                 (if (= book-page-no nil)
                    {:status 200 :body {book-title "No Result Found..."}}
                    (do
                      (let [updated-wikipedia-URL (str summary-url (spaces->underscores (get available-pages book-page-no)))
                            search-results (json/read-str (:body (client/get updated-wikipedia-URL)))
                            summary (summary-from-Wikipedia-result search-results)]
                        (insert-summary-into-databse summary)
                        {:status 200 :body summary})))))))
      (do
        (insert-summary-into-databse summary)
        {:status 200 :body summary}))))