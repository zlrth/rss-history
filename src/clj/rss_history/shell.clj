(ns rss-history.shell
  (:require [clojure.set :refer [rename-keys]]
            [feedparser-clj.core :as feedparser]
            [clojure.java.io :as io]
            [clj-time.core :as time]
            [clojure.java.shell :as shell]))

"given params, run waybackpack, and return an rss feed"
"we want to create a "
"to make myself happy, i want to show the user an RSS feed in the browser"
"after"
(defn do-thing [params]
  (let [something 'somethign
        url (:url params)]
    (shell/sh "waybackpack" "-d" "temp" url)
    (str "donkey  " (:url params)))
  )
