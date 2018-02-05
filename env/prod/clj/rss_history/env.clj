(ns rss-history.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[rss-history started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[rss-history has shut down successfully]=-"))
   :middleware identity})
