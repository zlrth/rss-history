(ns rss-history.rss
  (:require [clojure.set :refer [rename-keys]]
            [feedparser-clj.core :as feedparser]
            [clojure.java.io :as io]
            [clj-time.core :as time]))

(def feeds  (->> "ribbonrss"
                 io/file
                 file-seq
                 (filter #(.isFile %))
                 (map #(.toURI %))
                 (map #(.toString %))
                 (map feedparser/parse-feed)))

(def undeduplicated  (->> feeds (map :entries) flatten))

(def deduplicated (into #{} undeduplicated))

(defn rename-rss-keys [set-of-feeds]
  (map (fn [x] (rename-keys x {:published-date :pubDate
                               :updated-date   :pubDate
                               :authors        :author
                               :categories     :category
                               :enclosures     :enclosure
                               :uri            :guid}))
       set-of-feeds))

(defn dissoc-rss-keys [set-of-feeds] ;; TODO un-dissoc :category
  (map #(dissoc % :contributors :contents :category) set-of-feeds))

#_(defn rejigger-description [set-of-feeds] ;; HACK terse. also 
  (map (fn [x] (update x :description :value)) set-of-feeds))
(defn rejigger-description-blogspot [set-of-feeds] ;; HACK terse
  (map (fn [x]
         (let [value (-> x :contents first :value)
               cdata (str "<![CDATA[" value "]]>")]
           (assoc x :description #_"<![CDATA[this is <b>bold</b>]]>" cdata ))) set-of-feeds))

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
  (clojure.walk/postwalk #(if-let [value (:description %)]
                            (assoc % :description (xml-str value))
                            %) entry))

#_ (as-> (feedparser/parse-feed "http://anonymousmugwump.blogspot.co.uk/feeds/posts/default?max-results=100") $
     (map rss-history.rss/escape-value-in-feed (:entries $))
     ())
