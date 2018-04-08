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
            [datomic.client.api :as d]))

(defn start []
  (mount/start-without #'rss-history.core/repl-server))

(defn stop []
  (mount/stop-except #'rss-history.core/repl-server))

(defn restart []
  (stop)
  (start))

(def cfg {:server-type :peer-server
          :access-key "myaccesskey"
          :secret "mysecret"
          :endpoint "localhost:4334"})

(def client (d/client cfg))

;; (def conn (d/connect client {:db-name "hello"}))

(def user-schema
  [{:db/ident :user/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "user name"}])
(def add-user
  {:user/name "matt"})
(def add-doc
  {:top/doc
   {:doc/owner [:user/name "matt"]
    :doc/fulltext "test"
    :doc/url "www.google.com"}})
(def add-derived-feed
  {:doc/fragments
   [{:doc/feedtext "te"}
    {:doc/feedtext "xt"}]})
(def doc-schema
  [{:db/ident :doc/url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "original URL of feed"}
   {:db/ident :doc/feed-url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "libby.io:blah/whatever/atom.xml"}
   {:db/ident :doc/date
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "original URL of feed"}
   {:db/ident :doc/fulltext
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "original URL of feed"}])
(def derived-feed-schema
  [{:db/ident :doc/fragments
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "ref type associating fragments to a feed"
    :db/isComponent true}
   {:db/ident :doc/feedtext
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "a feed item. one post."}])
