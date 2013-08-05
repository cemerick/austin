(ns austin.plugin
  (:require [clojure.java.io :as io]))

(def ^:private austin-version
  (-> (io/resource "META-INF/leiningen/com.cemerick/austin/project.clj")
       slurp
       read-string
       (nth 2)))

(assert (string? austin-version)
        (str "Something went wrong, version of austin is not a string: "
             austin-version))

(defn middleware
  [project]
  (-> project
      (update-in [:dependencies]
                 (fnil into [])
                 [['com.cemerick/austin austin-version]])
      (update-in [:repl-options :nrepl-middleware]
                 (fnil into [])
                 '[cemerick.piggieback/wrap-cljs-repl])
      (update-in [:injections]
                 (fnil into [])
                 '[(require '[cemerick.austin.repls
                              :refer (exec)
                              :rename {exec austin-exec}])])))
