(ns task-wikipedia-endpoint.routes.functions
    (:require
        [failjure.core :as f]
        [clj-http.client :as client]
        [clojure.data.json :as json]))

(defn validate-numbers
    ;;"This function is used to validate 2 numbers"
    ;;"It will check both the numbers are of integer type of not"
    [first-number second-number]
    (if (and (integer? first-number) (integer? second-number))
        (+ first-number second-number)
        (f/fail "Please Enter Valid Number")))

(defn add-two-numbers
    ;;"This function is used to add two integer numbers"
    ;;"accepts 2 integer parametrs first-number & second-number"
    ;;"returns the total of both number"
    [first-number second-number]
    (f/attempt-all [addition (validate-numbers first-number second-number)]
        {:status 200 :body {:total addition}}
     (f/when-failed [e]
         {:status 400 :body {:total 0}})))

(defn add-two-numbers-demo
    ;;"This function is used to add two integer numbers"
    ;;"accepts 2 integer parametrs first-number & second-number"
    ;;"returns the total of both number"
    [first-number second-number]
    (+ first-number second-number))

(defn take-book-from-shelf 
    ;;"This function is used to return the book-name"
    []
    (str "Hello"))

(def search-url "https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")
(def content-url "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=")
(def summary-url "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=")

(defn replace-space-with-underscore
    ;;This fuction is used replace space with underscore
    [book-title]
    (clojure.string/replace book-title #" " "_"))

(defn get-page-number-key
    ;;This function is used to get the page number key in the content URL
    ;;It accepts body content & returns the page-number
    [search-results]
    (nth (keys (get (get search-results "query") "pages")) 0))

(defn check-novel-wikipedia-results
    ;;This function is used to check whether any wikipedia link available with (novel) or not
    ;;It will accept the argument book-title & the it will append (novel) at the end of it
    ;;After that it will check seach URL wil give any results of it or not
    [book-title]
    (let [URL (str search-url book-title "(novel)")
          search-results (json/read-str (:body (client/get URL)))]
        (if (= 1 (count (get search-results 1)))
            (do
                (let [updated-URL (str summary-url (replace-space-with-underscore (get (get search-results 1) 0)))
                      search-results (json/read-str (:body (client/get updated-URL)))
                      summary (get (get (get (get search-results "query") "pages") (get-page-number-key search-results)) "extract")]
                summary))
            false)))

(defn get-all-search-results
    ;;This function is used to get the all the search results
    ;;This fuctions accepts book-title argument
    ;;It will return list of available pages if no of pages is greater then 1 else it will return ""
    [book-title]
    (let [URL (str search-url book-title)
          search-results (json/read-str (:body (client/get URL)))]
        (if (= 0 (count (get search-results 1)))
            0
            (get search-results 1))))

(defn check-all-search-results
    ;;This function is used to check all the available results present in search API
    ;;It will accept list of available results present in search API 
    ;;Then it will check evry string in content API that whether keywords are present or not upto gien threshhold
    [available-pages]
    (doseq [i available-pages])
    )

(defn get-book-summary
    ;;This function is used to get the summary of the book
    ;;It is taking one argument called book title
    [book-title]
    (let [summary (check-novel-wikipedia-results book-title)]
    (if (= false summary)
    (do
        (let [available-pages (get-all-search-results book-title)]
            (if (= 0 available-pages)
             {:status 200 :body {book-title "No Result Found..."}}
             (check-all-search-results available-pages))))
    {:status 200 :body {book-title summary}})))
    


;;(doseq [i (string/split (:body a) #",")]
;;#_=> (println i))
;;["The_Glass_Bead_Game"
;;["The Glass Bead Game"
;;"The Glass Bead Game (album)"]
;;[""
;;""]
;;["https://en.wikipedia.org/wiki/The_Glass_Bead_Game"
;;"https://en.wikipedia.org/wiki/The_Glass_Bead_Game_(album)"]]
;;(apply str (remove #((set chars) %) coll)))

;;(def a (json/read-str (:body (client/get "https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=The_Glass_Bead_Game"))))
;;(doseq [i a]
;;(println i))
;;The_Glass_Bead_Game
;;[The Glass Bead Game The Glass Bead Game (album)]
;;[ ]
;;[https://en.wikipedia.org/wiki/The_Glass_Bead_Game https://en.wikipedia.org/wiki/The_Glass_Bead_Game_(album)]
;;(get (get a 1) 0)
;;"The Glass Bead Game"

;;user=> (clojure.string/replace (get (get a 1) 0) #" " "_")
;;"The_Glass_Bead_Game"
