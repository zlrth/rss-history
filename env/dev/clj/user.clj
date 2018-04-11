(ns user
  (:require [mount.core :as mount]
            [com.rpl.specter :as specter]
            [clojure.pprint :as pp]
            [clojure.java.shell :as sh]
            [feedparser-clj.core :as feed]
            [clojure.walk :as w]
            [clj-rss.core :as rss]
            [feedparser-clj.core :as feedparser]
            [clojure.java.io :as io]
            [rss-history.rss :refer :all]
            rss-history.core
            [rss-history.db :as db]
            [rss-history.utils :as u]
            [datomic.api :as d]
            [datomic.client.api :as dclient]
            [rss-history.controller :as cont]))

(defn start []
  (mount/start-without #'rss-history.core/repl-server))

(defn stop []
  (mount/stop-except #'rss-history.core/repl-server))

(defn restart []
  (stop)
  (start))
