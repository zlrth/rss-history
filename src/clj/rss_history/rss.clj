(ns rss-history.rss
  (:require [clojure.set :refer [rename-keys]]
            [feedparser-clj.core :as feedparser]
            [clojure.java.io :as io]))

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
                               :authors        :author
                               :categories     :category
                               :enclosures     :enclosure
                               :uri            :guid}))
       set-of-feeds))

(defn dissoc-rss-keys [set-of-feeds] ;; TODO un-dissoc :category
  (map #(dissoc % :contributors :contents :category) set-of-feeds))

(defn rejigger-description [set-of-feeds] ;; HACK terse
  (map (fn [x] (update x :description :value)) set-of-feeds))
