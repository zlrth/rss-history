(ns core-test
  (:require
   [clojure.test :refer :all]
   [ring.mock.request :refer :all]
   [rss-history.handler :refer :all]
   [clojure.java.io :as io]
   [clj-rss.core :as rss]
   [feedparser-clj.core :as feedparser]
   [feedme :as fm]))

(deftest can-blah-get-a-feed-entries-from-a-fixture-page
  (let [feed (fm/parse "file:///Users/matt/hacking/rss-history/test/resources/rss.xml")]
    (is (= 20 (count (:entries feed))))))


(deftest can-do-stuff-with-fixture-data
  (let [feeds (->> "ribbonrss"
                   io/file
                   file-seq
                   (filter #(.isFile %))
                   (map #(.toURI %))
                   (map #(.toString %))
                   (map feedparser/parse-feed))
        undeduplicated (->> feeds
                            (map :entries)
                            flatten)
        deduplicated (into #{} undeduplicated)]
    (testing "can produce correct number of feeds"
      (is (= 2820 (count undeduplicated))))
    (testing "can dedupe feeds"
      (is (= 624  (count deduplicated))))
    #_(testing "can produce rss feed"
      (is (= "something" (rss/channel (first deduplicated))))
     )))


#_(deftest can-produce-rss-feed
  )
