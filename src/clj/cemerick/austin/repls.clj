(ns ^{:doc "Convenience functions for starting REPLs assuming an nREPL +
           piggieback toolchain."}
  cemerick.austin.repls
  (:require [cemerick.austin :refer (exec-env)]
            [cemerick.piggieback :refer (cljs-repl)]))

(defn start
  [& exec-env-args]
  ;; TODO should we implicitly merge in :libs [""]?
  ;; http://dev.clojure.org/jira/browse/CLJS-526 &
  ;; http://dev.clojure.org/jira/browse/CLJS-521
  (cljs-repl :repl-env (doto (apply exec-env exec-env-args)
                         ; TODO https://github.com/cemerick/piggieback/issues/10
                         cljs.repl/-setup)))


