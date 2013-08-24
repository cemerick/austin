# Leiningen Integration

Austin can be started with Leiningen via the `austin` task. The `austin` task requires one of two subtasks, `project` or `browser`, which correspond to Austin's two ClojureScript REPL environments.

### project

`project` creates, in Austin parlance, a project REPL environment (`cemerick.austin/exec-env`.) To launch the simplest form of a project REPL use:

    $ lein trampoline austin project

`project` supports `exec-env`'s two paraments `:phantom-cmd` and `:exec-cmds`. These can either be specified in the `austin` section of project.clj or on the command line. An example of specifying them on the command line is:

    $ lein trampoline austin project :phantom-cmd slimerjs :exec-cmds '["open" "-ga" "/Applications/Google Chrome.app"]'

In project.clj, the above would be represented as:

    :austin {:phantom-cmd "slimerjs"
              :exec-cmds ["open" "-ga" "/Applications/Google Chrome.app"]}
                 
Any options specified on the command line will override those specified in project.clj.                 
                 
### browser

`browser` creates, in Austin parlance, a browser REPL environment (`cemerick.austin/repl-env`.) To launch it use:

    $ lein trampoline austin browser
    
`browser` requires a function to start the app. This function should be specified in the `austin` section of project.clj under the `:start-up` key. For example:

    :austin {:start-up (cemerick.austin.bcrepl-sample/run)}

## Example

[The example project's project.clj](browser-connected-repl-sample/project.clj) contains an example of the options.
