(ns user
  (:require [mount.core :as mount]
            [rss-history.figwheel :refer [start-fw stop-fw cljs]]
            [clojure.java.shell :as sh]
            [feedparser-clj.core :as feed]
            [clojure.walk :as w]
            [clj-rss.core :as rss]
            rss-history.core))

(defn start []
  (mount/start-without #'rss-history.core/repl-server))

(defn stop []
  (mount/stop-except #'rss-history.core/repl-server))

(defn restart []
  (stop)
  (start))


(def files (clojure.java.io/file "./ribbonrss"))

(def trig (for [x (filter #(= (.getName %) "Ribbonfarm") (file-seq files))] (feedparser-clj.core/parse-feed x)))

#_(def longlist (map  (fn [xx] (let [results (atom [])]
                                 (clojure.walk/postwalk
                                  #(do (if-let [uid (:contents %)] (swap! results conj uid)) %)
                                  xx)
                                 @results)) trig))
(def vlist (map (fn [xx] (let [results (atom [])]
                           (clojure.walk/postwalk
                            #(do (if-let [uid (:value %)] (swap! results conj uid)) %)
                            xx)
                           @results)) trig))


(def flattened (sort (distinct (flatten vlist))))
(def set-flattened (set flattened))
;; for each thing in :entries, if it's in (sort (distinct (flattened-vlist))), conj it, and get rid of it in (s (d f))).

;; map over (:entries (first trig)), if :value of one of them is (second vlist), do a thing

;; structure
["root"
 :entries
 [:description [:value "text"]
  [:description [:value "text"]]]]

#_ (let [celebrator (atom [])]
     (doseq [root trig]
       (doseq [entry (:entries root)]
         (let [c (:value (:description entry))]
           (if (= c (first (second vlist))) (swap! celebrator conj entry)))))
     @celebrator)


(def example-data [{:entries [{:content "1"} {:content "2"} {:shouldnt "red"}]}])

(defn what [xx]
  (let [results (atom [])]
    (clojure.walk/prewalk
     #(do (if-let [value %] (swap! results conj value)) (println %))
     xx)
    @results))

(defn no-red [doc]
  (w/prewalk (fn [n]
               (if (map? n)
                 (if (some #(= "red" %) (vals n))
                   {}
                   n)
                 n)) doc))


#_(defn total []
  (let [t (atom 0)]
    (w/prewalk #(if (number? %) (swap! t + %) %) (no-red doc))
    @t))

(def big-thing (let [celebrator (atom [])]
                 (doseq [root trig]
                   (doseq [entry (:entries root)]
                     (let [c (:value (:description entry))]
                       (if (contains? set-flattened c) (swap! celebrator conj entry)))))
                 @celebrator))

(def gr (group-by (comp :value :description) big-thing))

(def possibly-correct-589 (map (comp first val) gr))

;; replace \n with literal enter, replace \" with "

(defn group-by-better [key-f val-f data]
  (reduce (fn [m d]
            (let [k (key-f d)
                  v (get m k [])]
              (assoc m k (conj v (val-f d)))))
          {}
          data))
