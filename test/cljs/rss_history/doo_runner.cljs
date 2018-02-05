(ns rss-history.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [rss-history.core-test]))

(doo-tests 'rss-history.core-test)

