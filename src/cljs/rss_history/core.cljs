(ns rss-history.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [rss-history.ajax :refer [load-interceptors!]]
            [rss-history.handlers]
            [rss-history.subscriptions])
  (:import goog.History))

(defn nav-link [uri title page collapsed?]
  (let [selected-page (rf/subscribe [:page])]
    [:li.nav-item
     {:class (when (= page @selected-page) "active")}
     [:a.nav-link
      {:href uri
       :on-click #(reset! collapsed? true)} title]]))

(defn navbar []
  (r/with-let [collapsed? (r/atom true)]
    [:nav.navbar.navbar-dark.bg-primary
     [:button.navbar-toggler.hidden-sm-up
      {:on-click #(swap! collapsed? not)} "☰"]
     [:div.collapse.navbar-toggleable-xs
      (when-not @collapsed? {:class "in"})
      [:a.navbar-brand {:href "#/"} "rss-history"]
      [:ul.nav.navbar-nav
       [nav-link "#/" "Home" :home collapsed?]
       [nav-link "#/about" "About" :about collapsed?]]]]))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn home-page []
  [:div.container
   "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0' xmlns:atom='http://www.w3.org/2005/Atom'>\n<channel>\n<atom:link href='http://unqualified-reservations.blogspot.com' rel='self' type='application/rss+xml'/>\n<title>\nLung\n</title>\n<link>\nhttp://unqualified-reservations.blogspot.com\n</link>\n<description>\nhaving fun\n</description>\n<generator>\nclj-rss\n</generator>\n</channel>\n</rss>\n"])
(defn atom-input [value]
  (let [save (fn [] (rf/dispatch [:set-feed-url @value]))
        stop (fn [] (rf/dispatch [:set-feed-url ""]))]
    [:input {:type "text"
             :value @value
             :placeholder "bonk"
             :on-change #(reset! value (-> % .-target .-value))
             :on-key-down #(case (.-which %)
                             13 (save)
                             27 (stop)
                             nil)
             }])
  #_[:p "hello"]
  #_[:input
   {:type "button"
    :value "click mel"
    :on-click #(rf/dispatch [:set-feed-url @value])}])



#_(defn db-value []
  [])

(defn rss-page []
  (let [value (r/atom "")]
    [:div.container
     "enter something"
     [atom-input value]
     [:div
      [:p "the db value is" @(rf/subscribe [:feed-url])]]
     ]))

(defn test-page []
  [:div.container
   "dumb"])


(def pages
  {:home #'home-page
   :about #'about-page
   :rss #'rss-page
   :test #'test-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(secretary/defroute "/rss" []
  (rf/dispatch [:set-active-page :rss]))

(secretary/defroute "/test" []
  (rf/dispatch [:set-active-page :test]))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
