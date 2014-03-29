;; Copyright (c) Rich Hickey. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns cemerick.austin
  (:require [clojure.java.io :as io]
            [cljs.compiler :as comp]
            [cljs.closure :as cljsc]
            [cljs.env :as env]
            [cljs.repl :as repl])
  (:import cljs.repl.IJavaScriptEnv
           java.net.InetSocketAddress
           (com.sun.net.httpserver HttpServer HttpHandler HttpExchange)))

(declare handle-request)

(defn- create-server
  [port]
  (doto (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext "/" (reify HttpHandler
                          (handle [this req] (handle-request req))))
    (.setExecutor clojure.lang.Agent/soloExecutor)
    .start))

(defn default-server-port
  "Returns the port to which the Austin HTTP server will be bound by default,
either when first used or when restarted via nullary call to `start-server`.
The sources of configuration are, in order:
  
  * system property `cemerick.austin.default-server-port`
  * environment variable `AUSTIN_DEFAULT_SERVER_PORT`
  * default, 0 (autoselection of an open port)"
  []
  (Integer/parseInt (or (System/getProperty "cemerick.austin.default-server-port")
                        (System/getenv "AUSTIN_DEFAULT_SERVER_PORT")
                        "0")))

(defonce
  ^{:doc "A deref-able that contains the HTTP server that services all Austin REPLs."}
  server
  (delay (create-server (default-server-port))))

(defn stop-server
  "Stops the (assumed to be running) HTTP server that services all Austin REPLS."
  []
  (.stop @server 0))

(defn start-server
  "Starts the HTTP server that services all Austin REPLs.  Optionally takes a
[port] argument.  Does not stop any already-running HTTP server, see
`stop-server` for that.

Note that Austin automatically initializes its HTTP server as a side effect of
the first call to `get-browser-repl-port`; you don't have to call this
before using Austin, unless you need the server running on a particular port."
  ([]
    (start-server (default-server-port)))
  ([port]
   ; once people are explicitly starting the server, any derefable in `server`
   ; is fine
    (alter-var-root #'server (fn [_] (atom (create-server port))))))

(defn get-browser-repl-port
  "Returns the port to which the Austin HTTP server is bound.  If it is not
already running, it will be started as a side effect of the first call to this
function."
  []
  (-> @server .getAddress .getPort))

(defn- send-response
  [^HttpExchange ex status string-body & {:keys [content-type]
                                          :or {content-type "text/html"}}]
  (let [utf8 (.getBytes string-body "UTF-8")]
    (doto ex
      (-> .getResponseHeaders (.putAll {"Server" ["ClojureScript REPL"]
                                        "Content-Type" [(str content-type "; charset=utf-8")]}))
      (.sendResponseHeaders status (count utf8))
      (-> .getResponseBody (doto (.write utf8) .flush .close)))))

(defn- send-404
  [ex path]
  (send-response ex 404
    (str "<html><body>"
         "<h2>Page not found</h2>"
         "No page " path " found on this server."
         "</body></html>")))

(def ^:private session-init {:return-value-fn nil
                             :client-js nil
                             :loaded-libs #{}
                             :preloaded-libs #{}
                             :open-exchange nil
                             :exchange-promise nil
                             :ordering nil
                             :opts {}
                             :*out* nil})

(defonce ^:private sessions (atom {}))

(defn- deliver-exchange
  [session-id exch]
  (if-let [promise (-> @sessions (get session-id) :exchange-promise)]
    (do (swap! sessions update-in [session-id] assoc :open-exchange nil :exchange-promise nil)
        (deliver promise exch))
    (swap! sessions assoc-in [session-id :open-exchange] exch)))

(defn- open-exchange
  [session-id]
  (let [p (promise)]
    (if-let [exch (-> @sessions (get session-id) :open-exchange)]
      (do (swap! sessions assoc-in [session-id :open-exchange] nil)
        (deliver p exch))
      (swap! sessions assoc-in [session-id :exchange-promise] p))
    p))

(defn- set-return-value-fn
  "Save the return value function which will be called when the next
  return value is received."
  [session-id f]
  (swap! sessions (fn [old] (assoc-in old [session-id :return-value-fn] f))))

(defn- send-for-eval
  "Given a form and a return value function, send the form to the
  browser for evaluation. The return value function will be called
  when the return value is received."
  ([session-id form return-value-fn]
    (send-for-eval @(open-exchange session-id) session-id form return-value-fn))
  ([exch session-id form return-value-fn]
    (set-return-value-fn session-id return-value-fn)
    (send-response exch 200 form :content-type "text/javascript")))

(defn- return-value
  "Called by the server when a return value is received."
  [session-id val]
  (when-let [f (-> @sessions (get session-id) :return-value-fn)]
    (f val)))

(defn- repl-client-js [session-id]
  (if-let [session (get @sessions session-id)]
    (slurp @(:client-js session))
    ; TODO maybe we can make -tear-down yank the REPL environment out of
    ; browser-repl-env automatically?
    (format ";console.error('Austin ClojureScript REPL session %s does not exist. Maybe you have a stale ClojureScript REPL environment in `cemerick.austin.repls/browser-repl-env`?');"
            session-id)))

(defn- send-repl-client-page
  [^HttpExchange ex session-id]
  (let [url (format "http://%s/%s/repl" 
                    (-> ex .getRequestHeaders (get "Host") first)
                    session-id)]
    (send-response ex 200
      (str "<html><head><meta charset=\"UTF-8\"></head><body>
            <script type=\"text/javascript\">"
           (repl-client-js session-id)
           "</script>"
           "<script type=\"text/javascript\">
            clojure.browser.repl.client.start(" (pr-str url) ");
            </script>"
           "</body></html>"))))

(defn- send-repl-index
  [ex session-id]
  (let [url (format "http://%s/%s/repl"
                    (-> ex .getRequestHeaders (get "Host") first)
                    session-id)]
    (send-response ex 200
      (str "<html><head><meta charset=\"UTF-8\"></head><body>
            <script type=\"text/javascript\">"
           (repl-client-js session-id)
           "</script>"
           "<script type=\"text/javascript\">
            clojure.browser.repl.connect(" (pr-str url) ");
            </script>"
           "</body></html>"))))

(defn- send-static
  [ex session-id path]
  (let [opts (get @sessions session-id)
        st-dir (-> opts :opts :static-dir)]
    (if (and st-dir
          (not= "/favicon.ico" path))
      (let [path (if (= "/" path) "/index.html" path)]
        (if-let [local-path (seq (for [x (if (string? st-dir) [st-dir] st-dir)
                                       :when (.exists (io/file (str x path)))]
                                   (str x path)))]
          (send-response ex 200 (slurp (first local-path)) :content-type
            (condp #(.endsWith %2 %1) path
              ".html" "text/html"
              ".css" "text/css"
              ".html" "text/html"
              ".jpg" "image/jpeg"
              ".js" "text/javascript"
              ".png" "image/png"
              "text/plain"))
          (send-404 ex path)))
      (send-404 ex path))))

(defmulti ^:private handle-post :type)
  
(defmulti ^:private handle-get
  (fn [{:keys [http-exchange session-id path]}]
    (when session-id path)))

(defmethod handle-post :ready
  [{:keys [session-id http-exchange]}]
  (swap! sessions #(update-in % [session-id] merge
                     {:loaded-libs (-> % (get session-id) :preloaded-libs)
                      :ordering (agent {:expecting nil :fns {}})}))
  (env/with-compiler-env
    (-> @sessions (get session-id) :opts ::env/compiler)
    (send-for-eval http-exchange session-id
                   (cljsc/-compile
                    '[(ns cljs.user)
                      (set! *print-fn* clojure.browser.repl/repl-print)] {})
                   identity)))

(defn- add-in-order
  [{:keys [expecting fns]} order f]
  {:expecting (or expecting order) :fns (assoc fns order f)})

(defn- run-in-order
  [{:keys [expecting fns]}]
  (loop [order expecting
         fns fns]
    (if-let [f (get fns order)]
      (do (f)
          (recur (inc order) (dissoc fns order)))
      {:expecting order :fns fns})))

(defn- constrain-order
  "Elements to be printed in the REPL will arrive out of order. Ensure
  that they are printed in the correct order."
  [session-id order f]
  (doto (-> @sessions (get session-id) :ordering)
    (send-off add-in-order order f)
    (send-off run-in-order)))

(defmethod handle-post :print
  [{:keys [content order session-id http-exchange]}]
  (constrain-order session-id order
    (fn []
      (binding [*out* (-> @sessions (get session-id) :*out*)]
        (print (read-string content)))
      (.flush *out*)))
  (send-response http-exchange 200 "ignore__"))

(defmethod handle-post :result
  [{:keys [content order session-id http-exchange]}]
  (constrain-order session-id order
    (fn []
      (return-value session-id content)
      (deliver-exchange session-id http-exchange))))

(defn- request-path
  [^HttpExchange req]
  (-> req .getRequestURI .getPath))

(defmethod handle-get "/start"
  [{:keys [http-exchange session-id]}]
  (send-repl-index http-exchange session-id))

(defmethod handle-get :default
  [{:keys [http-exchange session-id path]}]
  (if session-id
    (if path
      (send-static http-exchange session-id path) 
      (send-repl-client-page http-exchange session-id))    
    (send-404 http-exchange (request-path http-exchange))))

(defn ^:private handle-request
  [^HttpExchange req]
  (let [[[_ session-id static-path]] (re-seq #"/(\d+)/repl(/.+)?" (request-path req))]
    (try
      (case (.getRequestMethod req)
        "GET" (handle-get {:path static-path
                           :session-id session-id
                           :http-exchange req})
        "POST" (handle-post (assoc (-> req .getRequestBody io/reader slurp read-string)
                                   :http-exchange req
                                   :session-id session-id)))
      (catch Throwable t (.printStackTrace t)))))

(defn- browser-eval
  "Given a string of JavaScript, evaluate it in the browser and return a map representing the
   result of the evaluation. The map will contain the keys :type and :value. :type can be
   :success, :exception, or :error. :success means that the JavaScript was evaluated without
   exception and :value will contain the return value of the evaluation. :exception means that
   there was an exception in the browser while evaluating the JavaScript and :value will
   contain the error message. :error means that some other error has occurred."
  [session-id form]
  (let [return-value (promise)]
    (send-for-eval session-id form (partial deliver return-value))
    (let [ret @return-value]
      (try (read-string ret)
           (catch Exception e
             {:status :error
              :value (str "Could not read return value: " ret)})))))

(defn- load-javascript
  "Accepts a REPL environment, a list of namespaces, and a URL for a
  JavaScript file which contains the implementation for the list of
  namespaces. Will load the JavaScript file into the REPL environment
  if any of the namespaces have not already been loaded from the
  ClojureScript REPL."
  [{:keys [session-id] :as repl-env} ns-list url]
  (let [missing (remove (-> @sessions (get session-id) :loaded-libs) ns-list)]
    (when (seq missing)
      (browser-eval session-id (slurp url))
      (swap! sessions update-in [session-id :loaded-libs] (partial apply conj) missing))))

(defrecord BrowserEnv []
  repl/IJavaScriptEnv
  (-setup [this]
    (swap! sessions update-in [(:session-id this) :*out*] (constantly *out*))
    (require 'cljs.repl.reflect)
    (repl/analyze-source (:src this))
    (comp/with-core-cljs))
  (-evaluate [this _ _ js] (browser-eval (:session-id this) js))
  (-load [this ns url] (load-javascript this ns url))
  (-tear-down [this]
    (swap! sessions dissoc (:session-id this))))

(defn- compile-client-js [opts]
  (cljsc/build '[(ns clojure.browser.repl.client
                   (:require [goog.events :as event]
                             [clojure.browser.repl :as repl]))
                 (defn start [url]
                   (event/listen js/window
                                 "load"
                                 (fn []
                                   (repl/start-evaluator url))))]
               ; the options value used as an ifn somewhere in cljs.closure :-/
               (-> (into {} opts)
                   ; TODO why isn't the :working-dir option for the brepl env
                   ; called :output-dir in the first place?
                   (assoc :output-dir (:working-dir opts))
                   (dissoc :source-map))))

(defn- create-client-js-file [opts file-path]
  (let [file (io/file file-path)]
    (when (not (.exists file))
      (spit file (with-out-str (compile-client-js (assoc opts :output-to :print)))))
    file))

(defn- provides-and-requires
  "Return a flat list of all provided and required namespaces from a
  sequence of IJavaScripts."
  [deps]
  (flatten (mapcat (juxt :provides :requires) deps)))

(defn- always-preload
  "Return a list of all namespaces which are always loaded into the browser
  when using a browser-connected REPL."
  []
  (let [cljs (provides-and-requires (cljsc/cljs-dependencies {} ["clojure.browser.repl"]))
        goog (provides-and-requires (cljsc/js-dependencies {} cljs))]
    (disj (set (concat cljs goog)) nil)))

(defn repl-env
  "Create a browser-connected REPL environment.

  Options:

  session-id:     The id of the (pre-existing) session to bind to
  working-dir:    The directory where the compiled REPL client JavaScript will
                  be stored. Defaults to \".repl\".
  serve-static:   Should the REPL server attempt to serve static content?
                  Defaults to true.
  static-dir:     List of directories to search for static content. Defaults to
                  [\".\" \"out/\"].
  preloaded-libs: List of namespaces that should not be sent from the REPL server
                  to the browser. This may be required if the browser is already
                  loading code and reloading it would cause a problem.
  optimizations:  The level of optimization to use when compiling the client
                  end of the REPL. Defaults to :simple.
  host:           The host URL on which austin will run the clojurescript repl.
                  Defaults to \"localhost\".
  src:            The source directory containing user-defined cljs files. Used to
                  support reflection. Defaults to \"src/\".
  "
  [& {:as opts}]
  {:pre [(or (not (contains? opts :session-id))
             (string? (:session-id opts)))]}
  (env/with-compiler-env (env/default-compiler-env opts)
    (let [opts (merge (BrowserEnv.)
                      {:optimizations :simple
                       :working-dir   ".repl"
                       :serve-static  true
                       :static-dir    ["." "out/"]
                       :preloaded-libs   []
                       :src           "src/"
                       :host          "localhost"
                       :source-map    true
                       :session-id (str (rand-int 9999))
                       ::env/compiler env/*compiler*}
                      opts)
          session-id (:session-id opts)
          repl-url (format "http://%s:%s/%s/repl" (:host opts) (get-browser-repl-port) session-id)
          opts (assoc opts
                 :repl-url repl-url
                 :entry-url (str repl-url "/start")
                 :working-dir (str (:working-dir opts) "/" session-id))
          preloaded-libs (set (concat (always-preload)
                                      (map str (:preloaded-libs opts))))]
      (swap! sessions update-in [session-id] #(merge %2 %)
             (assoc session-init
               :ordering (agent {:expecting nil :fns {}})
               :opts opts
               :preloaded-libs preloaded-libs
               :loaded-libs preloaded-libs
               :client-js (future (create-client-js-file
                                   opts
                                   (io/file (:working-dir opts) "client.js")))))
      (println (str "Browser-REPL ready @ " (:entry-url opts)))
      opts)))

; an IJavaScriptEnv that delegates to another [browser-env], but also manages
; the lifecycle of an external java.lang.Process that actually hosts evalution
(deftype DelegatingExecEnv [browser-env command ^:volatile-mutable process]
  cljs.repl/IJavaScriptEnv
  (-setup [this]
    (cljs.repl/-setup browser-env)
    (let [command (into-array String (concat command [(:entry-url browser-env)]))]
      (set! process (try
                      (.. Runtime getRuntime (exec command))
                      (catch Exception e
                        (throw (java.io.IOException.
                                (str "Failed to exec \"" (clojure.string/join " " command) "\"\n"
                                     "Error was:\n  " (.getMessage e) "\n")))))))
    this)
  (-evaluate [this a b c] (cljs.repl/-evaluate browser-env a b c))
  (-load [this ns url] (cljs.repl/-load browser-env ns url))
  (-tear-down [_]
    (cljs.repl/-tear-down browser-env)
    (.destroy process))

  clojure.lang.ILookup
  (valAt [_ k] (get browser-env k))
  (valAt [_ k default] (get browser-env k default))
  
  ; here so that (into {} env) will work, necessary for turning this env into cljsc option map
  clojure.lang.Seqable
  (seq [_] (seq browser-env)))

(defn exec-env*
  [browser-repl-env command+args]
  {:pre [(every? string? command+args)]}
  (DelegatingExecEnv. browser-repl-env command+args nil))

(defn exec-env
  "Create a browser-REPL environment backed by an external javascript runtime
launched via exec.

Accepts all of the arguments supported by `repl-env`,
plus an optional :exec-cmds value, which, if provided, must be a seq of strings
that constitute the command to be executed when the browser-REPL is set up.
(The :entry-url of the browser-repl will be passed as an additional argument in
this command.)  The default :exec-cmds is

  [\"phantomjs\" \"/path/to/generated-temp-phantomjs-script.js\"]

e.g. to start a browser-repl in the background using Chrome on OS X,
evaluate:

  (exec-env :exec-cmds [\"open\" \"-ga\" \"/Applications/Google Chrome.app\"])

If you are using a different _phantomjs-compatible_ headless browser
implementation (e.g. slimerjs, or perhaps your package manager installs
phantomjs with a different name?), you can pass the name of that binary
as :phantom-cmd, e.g.:

  (exec-env :phantom-cmd \"slimerjs\")"
  [& {:keys [exec-cmds phantom-cmd] :as args}]
  (let [exec-command (or exec-cmds
                       [(or phantom-cmd "phantomjs")
                        (let [f (doto (java.io.File/createTempFile "phantomjs_repl" ".js")
                                  .deleteOnExit
                                  (spit (str "var page = require('webpage').create();"
                                          "page.open(require('system').args[1]);")))]
                          (.getAbsolutePath f))])
        benv (apply repl-env (apply concat (dissoc args :exec-cmds)))]
    (exec-env* benv exec-command)))

;; TODO unconvinced of the utility of the reflection stuff
;; In any case, it can't be used here, explicitly depends on cljs.browser.repl
; Get reflection handlers hooked up. Presumably we want to do this all the time?
#_(require 'cljs.repl.reflect)

