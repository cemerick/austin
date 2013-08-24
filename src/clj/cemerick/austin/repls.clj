(ns cemerick.austin.repls
  (:require [cemerick.austin :refer (exec-env)]
            [clojure.tools.nrepl.middleware.interruptible-eval :as nrepl-eval]
            [cemerick.piggieback :as pb]
            cljs.repl))

(def browser-repl-env
  "An atom into which you can `reset!` the Austin REPL environment to which you
want your browser-connected REPLs to connect. (This is strictly a convenience,
you can easily achieve the same thing by other means without touching this
atom.)  A typical usage pattern might be:
  
In your nREPL (or other REPL implementation) session:
  
(def repl-env (reset! cemerick.austin.repls/browser-repl-env
                      (cemerick.austin/repl-env)))
(cemerick.austin.repls/cljs-repl repl-env)

And, somewhere in your webapp (demonstrating using hiccup markup, but whatever
you use to generate HTML will work):

[:html
 ; ... etc ...
 [:body [:script (cemerick.austin.repls/browser-connected-repl-js)]]]

`browser-connected-repl-js` uses the REPL environment in this atom to construct
a JavaScript string that will connect the browser runtime to that REPL environment
on load.

When you want your app to connect to a different REPL environment, just
`reset!` `cemerick.austin.repls/browser-repl-env` again."
  (atom nil))

(defn browser-connected-repl-js
  "Uses the REPL environment in `browser-repl-env` to construct
a JavaScript string that will connect the browser runtime to that REPL environment
on load.  See `browser-repl-env` docs for more."
  []
  (when-let [repl-url (:repl-url @browser-repl-env)]
    (format ";goog.require('clojure.browser.repl');clojure.browser.repl.connect.call(null, '%s');"
            repl-url)))

(defn cljs-repl
  "Same as `cljs.repl/repl`, except will use the appropriate REPL entry point
(Piggieback's `cljs-repl` or `cljs.repl/repl`) based on the the current
environment (i.e. whether nREPL is being used or not, respectively)."
  [repl-env & options]
 (if (thread-bound? #'nrepl-eval/*msg*)
    (apply pb/cljs-repl :repl-env repl-env options)
    (apply cljs.repl/repl repl-env options)))

(defn exec
  "Starts a ClojureScript REPL using Austin's `exec-env` REPL environment
using `cljs-repl` in this namespace (and so can be used whether you're using
nREPL or not).  All arguments are passed on to `exec-env` without
modification."
  [& exec-env-args]
  (cljs-repl (apply exec-env exec-env-args)))
