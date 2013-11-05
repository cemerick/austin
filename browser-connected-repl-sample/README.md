# Sample project demonstrating real-world usage of [Austin](http://github.com/cemerick/austin)'s browser-connected REPL support

If you're reading this on github, feel free to follow along, but it'll
be a lot more fun if you clone [the
repo](http://github.com/cemerick/austin) and get your hands dirty!

Austin is a really just a significant refactoring of ClojureScript's
standard browser-REPL, so [all of its
tutorials](https://github.com/clojure/clojurescript/wiki/The-REPL-and-Evaluation-Environments)
generally apply to Austin as well. However, Austin provides a workflow
that I personally find much easier to use, especially if I want to have
multiple browser-connected REPLs in flight at the same time.

## Running the sample app

Assuming you've cloned [Austin's
repo](https://github.com/cemerick/austin) to `$AUSTIN` (wherever that
is), do this:

1.  `cd` to `$AUSTIN/browser-connected-repl-sample`, and run:

        $ lein do cljsbuild once, repl

    This will compile the dummy sample ClojureScript namespace in
    `$AUSTIN/src/cljs`, which happens to require the ClojureScript
    browser-REPL client-side namespace.

2.  Once you're in the REPL (it will start up in the sample app's main
    namespace, `cemerick.austin.bcrepl-sample`), evaluate `(run)`. That
    just starts jetty on port `8080`; if you open a browser to [that
    server](http://localhost:8080), you'll see this page (which the
    sample re-uses as its only content).
3.  Create a new Austin ClojureScript REPL environment, like so:

        (def repl-env (reset! cemerick.austin.repls/browser-repl-env
                              (cemerick.austin/repl-env)))

    This also `reset!`'s the new REPL environment into the
    `browser-repl-env` atom. The sample app uses code like this:

        [:script (cemerick.austin.repls/browser-connected-repl-js)]

    to drop a snippet of JavaScript into the page that will cause the
    browser-REPL to connect to whichever REPL environment is in
    `browser-repl-env`; your app should do likewise. Be sure to load
    the code returned by the `(browser-connected-repl-js)` call as the
    last JavaScript loaded by your app's page.

    Note that for snippet to work, you'll need to have a `cljs` file in
    your project that requires `clojure.browser.repl`. This project's
    `cljs` file [has exactly that](https://github.com/cemerick/austin/blob/master/browser-connected-repl-sample/src/cljs/cemerick/austin/bcrepl_sample.cljs) in the ns declaration:
    
        (ns cemerick.austin.bcrepl-sample
          (:require [clojure.browser.repl]))
    
4.  Turn your Clojure REPL into a ClojureScript REPL tied to that REPL
    environment with

        (cemerick.austin.repls/cljs-repl repl-env)

5.  Now that the ClojureScript REPL is ready, you need to load
    [http://localhost:8080](http://localhost:8080), or reload it if you
    brought it up before the REPL environment was created and `reset!`
    into the `browser-repl-env` atom. Once you do that, evaluate some
    ClojureScript to make sure your shiny new REPL is working, e.g.

        (js/alert "Salut!")

    (Note: if you see no response, try temporarily disabling browser
    extensions. A problem has been seen where the Google Voice
    extension in Chrome somehow prevents the browser from listening
    for packets from the REPL. See the full discussion in [this
    issue](https://github.com/cemerick/austin/issues/17).

You can reload your app's page as many times as you like; it will
re-connect on each page load to the same REPL environment. If you want
to connect to a *different* REPL environment, just put the it into
`browser-repl-env` prior to loading the page you'd like to have
connected to it. At some point, Austin may provide a bit of
ClojureScript that will allow you to choose (from within the browser)
which REPL environment to which you'd like to connect…
