(ns user
  (:require [mount.core :as mount]
            [rss-history.figwheel :refer [start-fw stop-fw cljs]]
            [clojure.java.shell :as sh]
            [feedparser-clj.core :as feed]
            [clojure.walk :as w]
            [clj-rss.core :as rss]
            [feedparser-clj.core :as feedparser]
            [clojure.java.io :as io]
            [rss-history.rss :refer :all]
            rss-history.core))

(defn start []
  (mount/start-without #'rss-history.core/repl-server))

(defn stop []
  (mount/stop-except #'rss-history.core/repl-server))

(defn restart []
  (stop)
  (start))
