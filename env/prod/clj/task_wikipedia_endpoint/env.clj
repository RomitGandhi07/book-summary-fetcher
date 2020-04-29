(ns task-wikipedia-endpoint.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[task-wikipedia-endpoint started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[task-wikipedia-endpoint has shut down successfully]=-"))
   :middleware identity})
