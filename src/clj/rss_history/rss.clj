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
            [clojure.edn :as edn]
            [clj-time.periodic :as periodic]
            [clj-time.coerce :as time-coerce]))

(def uri "datomic:dev://localhost:4334/hello3") ;; for free


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


(defn get-full-text [user url]
  (->>  (d/q '[:find ?fulltext .
               :in $ ?user ?url
               :where
               [?e :user/name ?user]
               [?e :doc/url ?url]
               [?e :doc/fulltext ?fulltext]]
             (d/db (d/connect uri)) user url)
        edn/read-string))

(defn get-all-fragments-and-timestamps [user url]
  (->> (d/q '[:find ?feedtexts ?timestamps
              :in $ ?user ?url
              :where
              [?e :fragment/owner ?user]
              [?e :fragment/rooturl ?url]
              [?e :fragment/timestamp ?timestamps]
              [?e :fragment/feedtext ?feedtexts]]
            (d/db (d/connect uri)) [:user/name user] url)
       ))


(def time->days {"9"     365
                 9   365 ;; javascript wtf?
                 "7" 182
                 7 182
                 "5" 91
                 5 91
                 "3"  30
                 3 30
                 "1"   7
                 1 7})


(defn entries->first-feed [entries time]
  (let [entries-per-day (/ (count entries) (get time->days time))] ;; this produces a clojure.lang.Ratio
    [entries-per-day (take entries-per-day entries)]) )


(defn produce-feed
  "HACK FIXME TODO "
  [user url time]
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

(defn calculate-seconds
  "Given a number of "
  [num-days num-entries] ;; 182, 100 means "in half a year, go through 100 entries. 
  (let [seconds-in-time-period  (* (get time->days num-days) 86400)
        seconds-between-entries (/ seconds-in-time-period num-entries)]
    seconds-between-entries))

(defn make-map [fragment timestamp user url]
  {:fragment/feedtext (str fragment)
   :fragment/timestamp (time-coerce/to-date timestamp)
   :fragment/owner [:user/name user]
   :fragment/rooturl url
   :fragment/hash [:doc/hash ] })

(defn put-all-fragments-into-db-with-timestamps [user url time]
  (let [fulltext (get-full-text user url) ;; assumes fulltext has desired order of fragments
        num-fragments (-> fulltext :entries count)
        seconds       (calculate-seconds time num-fragments)
        timestamps     (take num-fragments (periodic/periodic-seq (time/now) (time/seconds seconds)))
        tx (vec (mapv make-map (-> fulltext :entries) timestamps (repeat user) (repeat url)))]
    (dclient/transact db/db-conn {:tx-data tx})))

(defn db-query->the-feeds [s the-time]
  (let [list-of-shit (->>  (group-by second s)
                           (filter #(time/before? the-time (clj-time.coerce/to-date-time (first  %)) ))
                           (sort-by first)
                           (take 20)
                           (map (comp read-string ffirst val)))
        elsee        (->> list-of-shit
                          rejigger-description-blogspot
                          rename-rss-keys
                          dissoc-rss-keys)]
    elsee
    ))

(defn the-feeds->derived-rss-feed [s]
  (clj-rss/channel-xml {:title "test"
                        :link "testlink"
                        :description "testdescription"}
                       s)
  )
