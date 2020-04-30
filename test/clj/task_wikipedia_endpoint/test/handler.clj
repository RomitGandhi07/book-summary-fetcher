(ns task-wikipedia-endpoint.test.handler
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [cheshire.core :as cheshire]
    [task-wikipedia-endpoint.handler :refer :all]
    [task-wikipedia-endpoint.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'task-wikipedia-endpoint.config/env
                 #'task-wikipedia-endpoint.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response)))))
    
  (testing "services"
    (testing "GET: api/add?x=[first-value]&y=[second-value]"
      (testing "success"
        (let [response ((app) (request :get "/api/add?x=20&y=50"))
            body     (parse-body (:body response))]
            (is (= (:status response) 200))
            (is (= (:total body) 70))))
            
      (testing "bad-request (second number is string instead of integer)"
        (let [response ((app) (request :get "/api/add?x=20&y=abc"))]
            (is (= (:status response) 400)))))

    (testing "GET: api/book"
      (testing "success"
        (let [response ((app) (request :get "/api/book"))
            body     (parse-body (:body response))]
            (is (= (:status response) 200))
            (is (= (:name body) "Hello")))))))
