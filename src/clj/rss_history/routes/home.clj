(ns rss-history.routes.home
  (:require [rss-history.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [rss-history.controller :as controller]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/user/:user/:title" [user title :as request]
       (-> (response/ok  "<xml> fhfeowi</xml>" #_(controller/get-derived-feed user title))
           (response/header "Content-Type" "application/rss+xml, application/rdf+xml;q=0.8, application/atom+xml;q=0.6, application/xml;q=0.4, text/xml;q=0.4"))) ;; https://stackoverflow.com/a/7001617/3925569

  (GET "/" []
       (home-page))

  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))

  (GET "/matt" []
       (-> (response/ok (-> "xml.xml" slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))

  (POST "/rss" request
        (let [url (-> request :body-params)
              response (controller/do-thing url)]
          (-> (response/ok (str response  "\n"))
              (response/header "Content-Type" "text/plain; charset=utf-8"))))

  (POST "/generatefeed" request
        (let [url (-> request :body-params)
              response (controller/do-second-thing url)]
          (-> (response/ok (str response  "\n"))
              (response/header "Content-Type" "text/plain; charset=utf-8")))))
