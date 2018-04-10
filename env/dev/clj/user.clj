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
(def uri "datomic:dev://localhost:4334/hello") ;; for free
#_(d/create-database uri) ;; for free

#_(datomic.api/connect uri)
#_(def conn (d/connect uri)) ;; for free




(def cfg {:server-type :peer-server
          :access-key "myaccesskey"
          :secret "mysecret"
          :endpoint "localhost:8998"}) ;; not sure about this change

;; (def client (dclient/client cfg))
;; for some reason, i can't uncomment this

;;  (def conn (d/connect client {:db-name "hello"})) ;; for some reason, i can't uncomment this

(def user-schema
  [{;; :db/id #db/id[:db.part/db]
    :db/ident :user/name
    :db/unique :db.unique/identity
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "user name"
    :db.install/_attribute :db.part/db
    }])
(def add-user
  [{:user/name "chonk"}])
(def add-doc
  [{:doc/user [:user/name "matt"]
     :doc/fulltext "test"
     :doc/url "www.google.com"}])
(def add-derived-feed
  {:doc/fragments
   [{:doc/feedtext "te"}
    {:doc/feedtext "xt"}]})
(def doc-schema
  [{:db/id #db/id[:db.part/db]
    :db/ident :doc/url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "original URL of feed"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :doc/user
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "each doc has one user"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :doc/feed-url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "libby.io:blah/whatever/atom.xml"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :doc/date
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "original URL of feed"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :doc/fulltext
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "fulltext of the feed"
    :db.install/_attribute :db.part/db}])

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



(defn add-feed-url-and-user-to-db! [feed user url]
  (let [tx [{:doc/user user
             :doc/url  url
             :doc/fulltext feed}]]
    (d/transact db/db-conn tx)))
