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
  [params]
  (let [url             (:url params)
        name            (:name params)
        feed            (feedparser/parse-feed (utils/format-url url))
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
  [params]
  (let [_ (log/info params)
        user (:user params)
        url  (:url params)
        time (:partitionLength params)
        feed (rss-history.rss/produce-feed user url time)]
    (db/add-first-feed-to-db! feed user url)
    (publish url)))


