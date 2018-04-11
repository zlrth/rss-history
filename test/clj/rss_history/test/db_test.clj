(ns rss-history.test.db-test
  (:require
   [clojure.test :refer :all]
   [ring.mock.request :refer :all]
   [rss-history.rss :refer :all]
   [rss-history.db :refer :all]
   [clojure.java.io :as io]
   [clj-rss.core :as rss]
   [feedparser-clj.core :as feedparser]))

(deftest test-add-doc
  (is (= 1 1)))

