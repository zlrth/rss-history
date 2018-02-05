(ns rss-history.app
  (:require [rss-history.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
