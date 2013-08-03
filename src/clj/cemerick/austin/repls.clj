(ns cemerick.austin.repls
  (:require [cemerick.austin :refer (exec-env)]
            [clojure.tools.nrepl.middleware.interruptible-eval :as nrepl-eval]
            [cemerick.piggieback :refer (cljs-repl)]
            cljs.repl))

(defn exec
  [& exec-env-args]
  (let [env (apply exec-env exec-env-args)]
    (if (thread-bound? #'nrepl-eval/*msg*)
    (cljs-repl :repl-env env)
    (cljs.repl/repl env))))
