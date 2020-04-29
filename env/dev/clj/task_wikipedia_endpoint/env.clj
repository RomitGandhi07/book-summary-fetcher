(ns task-wikipedia-endpoint.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [task-wikipedia-endpoint.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[task-wikipedia-endpoint started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[task-wikipedia-endpoint has shut down successfully]=-"))
   :middleware wrap-dev})
