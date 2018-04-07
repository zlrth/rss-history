(ns rss-history.rss
  (:require [clojure.walk :as walk]
            [clojure.set :refer [rename-keys]]
            [feedparser-clj.core :as feedparser]
            [clojure.java.io :as io]
            [clj-time.core :as time]
            [clj-rss.core :as clj-rss]
            [rss-history.utils :as u]))



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

#_ (as-> (feedparser/parse-feed "http://anonymousmugwump.blogspot.co.uk/feeds/posts/default?max-results=100") $
     (map rss-history.rss/escape-value-in-feed (:entries $))
     ())
#_(->> (:entries 
      (feedparser/parse-feed "http://unqualified-reservations.blogspot.com/feeds/posts/default?max-results=10"))
     rejigger-description-blogspot
     rename-rss-keys
     dissoc-rss-keys
     (take 10)
     (rss/channel-xml {:title "moldbug cdata first"
                       :link "link"
                       :description "descriptionfoobar"})
     (spit "withescaping.xml"))

(defn get-entries [db user url]
  (->> db
       (u/match-kv :user user)
       :docs
       (u/match-kv :url url)
       :fulltext
       :entries))

(defn get-full-text [db user url]
  (->> db
       (u/match-kv :user user)
       :docs
       (u/match-kv :url url)
       :fulltext))

(def time->days {"a year"     365
                 "6 months" 182
                 "3 months" 91
                 "a month"  30
                 "a week"   7})

(defn entries->first-feed [entries time]
  (let [entries-per-day (/ (count entries) (get time->days time))] ;; this produces a clojure.lang.Ratio
    [entries-per-day (take entries-per-day entries)]) )

(defn produce-feed [db user url time]
  (let [full-text (get-full-text db user url)
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
                               entries)
         (spit "cancreate/potential-feed.xml"))))

#_(let [[t-me {:keys [docs] :as t-you}]
      [{:user "me"  :docs [{:one "two"}]}
       {:user "you" :docs [{:three "four"}]}]
      payload {:five "six"}
      new-you-docs (conj docs payload)]
  [t-me
   (assoc t-you :docs new-you-docs)])

(def attempt  (fn [coll]
                (map
                 (fn [row]
                   (if (= (:user row) "matt")
                     (assoc-in row [:docs]  (conj (:docs row) {:five "six"}))
                     row))  coll)))
#_(->>  ((fn append-to-users-docs [user url coll]
         (map
          (fn [row]
            (if (= (:user row) user)
              (map #(if (= (:url %) url)
                      (assoc-in row [:docs] (conj (:docs row) {:five "six"}))
                      row)
                   (:docs row))
              
              row))  coll)) "matt" "www.bing.com" dummy-data )
      clojure.pprint/pprint)
