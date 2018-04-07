(ns rss-history.dates
  (:require [clj-time.core :as time]
            [rss-history.rss :as rss]
            ))
;; partition posts by dates

;; user should be able to march ahead. should be able to see:
;; on this date, what posts will i see?
;; this function returns a map of {timestamp, [posts], timestamp [posts]}
;; to be put into the db.
(defn something [entries time-period])

