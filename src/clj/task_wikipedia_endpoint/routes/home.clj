(ns task-wikipedia-endpoint.routes.home
  (:require
   [task-wikipedia-endpoint.layout :as layout]
   [task-wikipedia-endpoint.db.core :as db]
   [clojure.java.io :as io]
   [task-wikipedia-endpoint.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [request]
  (layout/render request "about.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]])

