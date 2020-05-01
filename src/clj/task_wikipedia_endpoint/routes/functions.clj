(ns task-wikipedia-endpoint.routes.functions
    (:require
        [failjure.core :as f]
        [clj-http.client :as client]))

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

(defn get-book-summary
    ;;This function is used to get the summary of the book
    ;;It is taking one argument called book title
    [book-title]
    {:status 200 :body {book-title book-title}})

(defn check-book-exist
    ;;"This function is used to check whether the book title page is available in the wikipedia or not"
    ;;"It will take one argument->book-title"
    ;;"Then it will check using wikipedia API that similar pages of that keyword in the wikipedia is present or not"
    [book-title]
    (let [URL (str "https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=" book-title)
          response (client/get URL)]
          (:body response)))

    


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