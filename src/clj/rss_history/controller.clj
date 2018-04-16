(ns rss-history.controller
  (:require [clojure.set :refer [rename-keys]]
            [clojure.tools.logging :as log]
            [feedparser-clj.core :as feedparser]
            [clojure.java.io :as io]
            [clj-time.core :as time]
            [clojure.data.json :as json]
            [clojure.java.shell :as shell]
            [rss-history.db :as db]
            [rss-history.utils :as utils]))

"want to give back to the user: there are X number of posts. how would you like them?"
(defn do-thing
  "In the first interaction the user has with the app, we are given their name,
  and the URL they want. We parse the feed, and tell the user how many posts
  there are, to help them decide how they want their feed."
  [{:keys [url name] :as params}]
  (let [feed            (feedparser/parse-feed (utils/format-url url))
        derecordized    (clojure.walk/postwalk #(if (record? %) (into {} %) %) feed) ;; HACK records don't deserialize via the standard deserializer. Maybe  ptaoussanis/nippy would work?
        number-of-posts (count (:entries derecordized))]
    (db/add-feed-url-and-user-to-db! derecordized name url)
    (log/info {:postCount number-of-posts})
    (json/write-str {:postCount number-of-posts})))


(defn do-second-thing
  "params is a map
  [{:type \"forever\" or \"finisher\"}, \"year\" | \"1.5\"]
  Then we partition the feed and and return
  the feed URL
  the end date if :type \"finisher\"
  for now, we're only implementing \"finisher\" "
  [{:keys [user url partitionLength] :as params}]
  (let [feed-id (rss-history.rss/put-all-fragments-into-db-with-timestamps user url partitionLength)]
    (json/write-str {:success (str  "localhost:8081/user/" user "/" feed-id) })))



(defn get-derived-feed [user feed-hash]
  "we get all feed-ids by getting all feeds associated with a user, we hash all of them, we return the feed"
  (->> (rss-history.rss/get-all-fragments-and-timestamps user feed-hash)
       (rss-history.rss/db-query->the-feeds (time/now))
       (rss-history.rss/the-feeds->derived-rss-feed)))

