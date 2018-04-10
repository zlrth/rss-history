(ns rss-history.db
  (:require [clojure.edn :as edn]
            [rss-history.utils :as u]
            [datomic.api :as d]
            [datomic.client.api :as dclient]
            #_[rss-history.core :refer [db-conn]]
            [mount.core :as mount]))

(def uri "datomic:dev://localhost:4334/hello")

(mount/defstate ^{:on-reload :noop}
  db-conn
  :start
  (do
    (d/create-database uri) ;; how to make sure transactor is on?
    (d/connect uri))
  :stop
  (when db-conn
    (d/shutdown false)))


(def state-atom (atom []))

(def dummy-data
  [{:user "matt"
    :docs [{:fulltext "the text"
            :url "www.google.com"
            :fragments [{:date "2017" :fragment "the"}
                        {:date "2018" :fragment " text"}]}
           {:fulltext "is good"
            :url "www.bing.com"
            :fragments [{:date "2017" :fragment "is"}
                        {:date "2018" :fragment " good"}]}]}
   {:user "ttam"
    :docs [{:fulltext "bark mark"
            :url "www.yahoo.com"
            :fragments [{:date "2017" :fragment "bark"}
                        {:date "2018" :fragment " mark"}]}]}])

(defn serialize! []
  (spit "state.edn" @state-atom))

(defn startup
  "Load 'database'. Empty string is the last form because (reset!) returns the new value, which is a large data structure, which locks up my repl when developing."
  []
  (reset! state-atom (edn/read-string (slurp "state.edn")))
  "")

#_(defn add-feed-url-and-user-to-db! [feed user url]
  (swap! state-atom conj {:user user :docs [{:fulltext feed :url url}]})
  (serialize!))


(defn add-feed-url-and-user-to-db! [feed user url]
  (let [tx [{:user/name user
             :doc/url url
             :doc/fulltext (str feed)}]]
    (d/transact db-conn tx)))


(defn add-first-feed-to-db! [feed user url]
  (swap! state-atom assoc ))
