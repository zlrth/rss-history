(ns rss-history.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [rss-history.layout :refer [error-page]]
            [rss-history.routes.home :refer [home-routes]]
            [compojure.route :as route]
            [rss-history.env :refer [defaults]]
            [mount.core :as mount]
            [rss-history.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        #_(wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
