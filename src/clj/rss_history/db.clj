(ns rss-history.db
  (:require [clojure.edn :as edn]
            [rss-history.utils :as u]
            [datomic.api :as d]
            [datomic.client.api :as dclient]
            #_[rss-history.core :refer [db-conn]]
            [mount.core :as mount]))

(def uri "datomic:dev://localhost:4334/hello2")
(def cfg {:server-type :peer-server
          :access-key "myaccesskey"
          :secret "mysecret"
          :endpoint "localhost:8998"})
(mount/defstate ^{:on-reload :noop}
  db-conn
  :start
  (do
    (dclient/connect (dclient/client cfg) {:db-name "hello2"})) ;; how to make sure transactor is on?
  :stop
  (when db-conn
    (d/shutdown false)))


(defn add-feed-url-and-user-to-db! [feed user url]
  (let [tx [{:user/name user
             :doc/url url
             :doc/fulltext (str feed)}]]
    (dclient/transact db-conn {:tx-data tx})))

(defn add-first-feed-to-db! [feed user url]
  nil
  )

;; add url: libby.io:8080/matt/title/feed
;; GET thatthng query datomic: given today's timestamp, serve the feed.

(def user-schema
  [{;; :db/id #db/id[:db.part/db]
    :db/ident :user/name
    :db/unique :db.unique/identity
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "user name"
    :db.install/_attribute :db.part/db
    }])


(def doc-schema
  [{;; :db/id #db/id[:db.part/db]
    :db/ident :doc/url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "original URL of feed"
    :db.install/_attribute :db.part/db}
   {;; :db/id #db/id[:db.part/db]
    :db/ident :doc/user
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "each doc has one user"
    :db.install/_attribute :db.part/db}
   {;; :db/id #db/id[:db.part/db]
    :db/ident :doc/feed-url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "libby.io:blah/whatever/atom.xml"
    :db.install/_attribute :db.part/db}
   {;; :db/id #db/id[:db.part/db]
    :db/ident :doc/date
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "original URL of feed"
    :db.install/_attribute :db.part/db}
   {;; :db/id #db/id[:db.part/db]
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
