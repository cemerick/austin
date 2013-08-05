(ns cemerick.austin.bcrepl-sample
  (:require [clojure.browser.repl]))

(defn hello
  []
  (js/alert "hello"))
