(ns rss-history.routes.home
  (:require [rss-history.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [rss-history.shell]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
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
              response (rss-history.shell/do-thing url)]

          (-> (response/ok (str response  "\n"))
              (response/header "Content-Type" "text/plain; charset=utf-8")))))

