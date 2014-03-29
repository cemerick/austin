(defproject com.cemerick/austin "0.1.5-SNAPSHOT"
  :description "The ClojureScript browser-repl, rebuilt stronger, faster, easier."
  :url "http://github.com/cemerick/austin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2197"]
                 [com.cemerick/piggieback "0.1.3"]]

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :repositories {"oss-public" "https://oss.sonatype.org/content/groups/public/"}

  :scm {:url "git@github.com:cemerick/austin.git"}
  :pom-addition [:developers [:developer
                              [:name "Chas Emerick"]
                              [:url "http://cemerick.com"]
                              [:email "chas@cemerick.com"]
                              [:timezone "-5"]]])
