(ns ^:figwheel-no-load rss-history.app
  (:require [rss-history.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
