(ns leiningen.austin
  (:require [leiningen.help :as lhelp]
            [leiningen.core.eval :as leval]
            [leiningen.core.main :as lmain]
            [leiningen.trampoline :as ltrampoline]))

;; Ripped off from cljsbuild
(defmacro require-trampoline [& forms]
  `(if ltrampoline/*trampoline?*
     (do ~@forms)
     (do
       (println "REPL subcommands must be run via \"lein trampoline austin <command>\".")
       (lmain/abort))))

;; Ripped off from cljsbuild
(defn run-local-project [project requires form]
  (leval/eval-in-project project
                         ; Without an explicit exit, the in-project subprocess seems to just hang for
                         ; around 30 seconds before exiting.  I don't fully understand why...
                         `(try
                            (do
                              ~form
                              (System/exit 0))
                            (catch Exception e#
                              (do
                                (.printStackTrace e#)
                                (System/exit 1))))
                         requires))

;; Ripped off from lein-ring.
(defn load-namespaces
  "Create require forms for each of the supplied symbols. This exists because
  Clojure cannot load and use a new namespace in the same eval form."
  [& syms]
  `(require
     ~@(for [s syms :when s]
         `'~(if-let [ns (namespace s)]
              (symbol ns)
              s))))

(defn- build-project-commands [options args]
       (let [phantom-cmd (or (args ":phantom-cmd") (options :phantom-cmd))
             exec-cmds (or (read-string (get args ":exec-cmds" "false")) (options :exec-cmds))
             cmds `(cemerick.austin.repls/cljs-repl (cemerick.austin/exec-env :phantom-cmd ~phantom-cmd :exec-cmds ~exec-cmds))]
         cmds))

(defn- project
       "Run an austin project REPL."
       [project options args]
       (require-trampoline
         (#'run-local-project
          project
          (load-namespaces 'cemerick.austin.repls)
          (build-project-commands options args))))

(defn- build-browser-commands [start-up]
       `(do
          ~@(list start-up)
          (def ~'repl-env (reset! cemerick.austin.repls/browser-repl-env (cemerick.austin/repl-env)))
          (cemerick.austin.repls/cljs-repl ~'repl-env)))

(defn- browser
       "Run an austin browser REPL."
       [project {start-up :start-up}]
       (require-trampoline
         (#'run-local-project
          project
          (load-namespaces (first start-up) 'cemerick.austin.repls 'cemerick.austin) ;; todo go through start-up recursively grabbing namespaces in case its in a do block
          (build-browser-commands start-up))))

(defn austin
  "Run the austin plugin."
  {:help-arglists '([repl-type])
   :subtasks [#'project #'browser]}
  ([proj]
   (println (lhelp/help-for "austin"))
   (lmain/abort))
  ([proj repl-type & args]
   (let [options (proj :austin)]
     (case repl-type
       "project" (project proj options (apply hash-map args))
       "browser" (browser proj options)
       (do
         (println
           "Subtask" (str \" repl-type \") "not found."
           (lhelp/subtask-help-for *ns* #'austin))
         (lmain/abort))))))
