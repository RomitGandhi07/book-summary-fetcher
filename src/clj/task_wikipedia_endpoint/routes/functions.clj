(ns task-wikipedia-endpoint.routes.functions
    (:require
        [failjure.core :as f]))

(defn validate-numbers
    ;;This function is used to validate 2 numbers
    ;;It will check both the numbers are of integer type of not
    [first-number second-number]
    (if (and (integer? first-number) (integer? second-number))
        (println "perfect")
        (f/fail "Please Enter Valid Number"))
    )

(defn add-two-numbers-demo 
    ;;This function is used to add two integer numbers
    ;;accepts 2 integer parametrs first-number & second-number
    ;;returns the total of both number
    [first-number second-number]
    (f/attempt-all [(validate-numbers first-number second-number)]
       (hash-map :status 200 :body (hash-map :total (+ first-number second-number))))
    (f/when-failed [e]
        (hash-map :status 400 :body (hash-map :total 0))))

(defn add-two-numbers 
    ;;This function is used to add two integer numbers
    ;;accepts 2 integer parametrs first-number & second-number
    ;;returns the total of both number
    [first-number second-number]
    (+ first-number second-number))

(defn take-book-from-shelf 
    ;;This function is used to return the book-name
    []
    (str "Hello"))