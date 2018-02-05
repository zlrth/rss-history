(ns rss-history.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [rss-history.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[rss-history started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[rss-history has shut down successfully]=-"))
   :middleware wrap-dev})
