(ns rss-history.rss
  (:require [clojure.walk :as walk]
            [clojure.set :refer [rename-keys]]
            [feedparser-clj.core :as feedparser]
            [clojure.java.io :as io]
            [clj-time.core :as time]
            [clj-rss.core :as clj-rss]
            [rss-history.utils :as u]
            [rss-history.db :as db]
            [datomic.client.api :as dclient]
            [datomic.api :as d]
            [clojure.edn :as edn]))

(def uri "datomic:dev://localhost:4334/hello2") ;; for free


(defn rename-rss-keys [set-of-feeds]
  (map (fn [x] (rename-keys x {:published-date :pubDate
                               :updated-date   :pubDate
                               :authors        :author
                               :categories     :category
                               :enclosures     :enclosure
                               :uri            :guid}))
       set-of-feeds))


(defn rejigger-description-blogspot
  "We're actually usually inputting atom feeds, but we're only ever outputting
  RSS2.0 feeds. This is a little lossy."
  [set-of-feeds]
  (map (fn [x]
         (let [value (-> x :contents first :value)
               cdata (str "<![CDATA[" value "]]>")]
           (assoc x :description cdata ))) set-of-feeds))

(defn dissoc-rss-keys
  "More conversion of atom to RSS2.0"
  [set-of-feeds] ;; TODO un-dissoc :category
  (map #(dissoc % :contributors :contents :category) set-of-feeds))

(defn xml-str
  "Returns a string suitable for inclusion as an XML element. If the string
  is wrapped in <![CDATA[ ... ]]>, do not escape special characters."
  [^String s]
  (if (and (.startsWith s "<![CDATA[")
           (.endsWith s "]]>"))
    s
    (if s
      (let [escapes #_{"&lt;" \< 
                     "&gt;" \> 
                     "&amp;" \& 
                     "&quot;" \" }
            {\< "&lt;",
                     \> "&gt;",
                     \& "&amp;",
                     \" "&quot;"}]
        (clojure.string/escape s escapes)))))

(defn escape-description-in-feed [entry]
  (walk/postwalk #(if-let [value (:description %)]
                            (assoc % :description (xml-str value))
                            %) entry))


(defn get-entries [db user url]
  (->> db
       (u/match-kv :user user)
       :docs
       (u/match-kv :url url)
       :fulltext
       :entries))

(defn get-full-text [user url]
  (->>  (d/q '[:find ?fulltext .
               :in $ ?user ?url
               :where
               [?e :user/name ?user]
               [?e :doc/url ?url]
               [?e :doc/fulltext ?fulltext]]
             (d/db (d/connect uri)) user url)
        edn/read-string))


#_(def time->days {"a year"     365
                 "6 months" 182
                 "3 months" 91
                 "a month"  30
                 "a week"   7})
(def time->days {"9"     365
                 "7" 182
                 "5" 91
                 "3"  30
                 "1"   7})

(defn entries->first-feed [entries time]
  (let [entries-per-day (/ (count entries) (get time->days time))] ;; this produces a clojure.lang.Ratio
    [entries-per-day (take entries-per-day entries)]) )

(defn produce-feed [user url time]
  (let [full-text (get-full-text user url)
        title     (str  (:title full-text) " -- Served by libby.rss!")
        link        (:link full-text)
        description (:link full-text)
        pre-processed-entries   (second  (entries->first-feed (:entries full-text) time))
        entries (-> pre-processed-entries
                    rejigger-description-blogspot
                    rename-rss-keys
                    dissoc-rss-keys)]
    (->>  (clj-rss/channel-xml {:title title
                               :link link
                               :description description}
                               entries))))

