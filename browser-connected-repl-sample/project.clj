(defproject com.cemerick/austin-repl-sample "NOT_DEPLOYED"
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [ring "1.2.2"]
                 [compojure "1.1.8"]
                 [enlive "1.1.5"]]

  :profiles {:dev {:repl-options {:init-ns cemerick.austin.bcrepl-sample}
                   :plugins [[com.cemerick/austin "0.1.4"]
                             [lein-cljsbuild "1.0.3"]]
                   :cljsbuild {:builds [{:source-paths ["src/cljs"]
                                         :compiler {:output-to "target/classes/public/app.js"
                                                    :optimizations :simple
                                                    :pretty-print true}}]}}})

