(ns rss-history.utils
  (:require [hickory.core :as hickory]
            [hickory.select :as hselect]))


(defn get-hickory [url]
  (->  url
       slurp
       hickory/parse
       hickory/as-hickory))

(defn find-blogger-url
  "given a hickory-tree of a blogspot homepage, find the blogger.com/.../$ID url"
  [hickory-tree]
  (->  (hselect/select (hselect/and
                        (hselect/tag :link)
                        (hselect/attr :rel #(= % "service.post")))
                       hickory-tree)
       first
       :attrs
       :href ;; :title is also here.
       ))


(defn format-url
  "Given a valid url of a blogspot blog, return a properly-formatted url for
  requesting all its posts."
  [url]
  (let [max-posts 9999
        hickory (get-hickory url)
        blogger-url (find-blogger-url hickory)]
    (str blogger-url "?max-results=" max-posts)))

(defn match-kv
  "Given a key, a value, and a collection of maps, return a list of the matching the key and value. The key-value pair must be unique. That is, no two :users named matt"
  [k v maps] ;; https://stackoverflow.com/a/30363888/3925569
  (first (filter (comp #{v} k) maps)) )
