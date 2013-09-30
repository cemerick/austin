(defproject com.cemerick/austin "0.1.2-SNAPSHOT"
  :description "The ClojureScript browser-repl, rebuilt stronger, faster, easier."
  :url "http://github.com/cemerick/austin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [com.cemerick/piggieback "0.1.0"]]

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  ;:plugins [[com.cemerick/austin "0.1.0-SNAPSHOT"]]

  :deploy-repositories {"releases"
                        {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                         :creds :gpg}
                        "snapshots"
                        {:url "https://oss.sonatype.org/content/repositories/snapshots/"
                         :creds :gpg}}

  ;;maven central requirements
  :scm {:url "git@github.com:cemerick/austin.git"}
  :pom-addition [:developers [:developer
                              [:name "Chas Emerick"]
                              [:url "http://cemerick.com"]
                              [:email "chas@cemerick.com"]
                              [:timezone "-5"]]])
