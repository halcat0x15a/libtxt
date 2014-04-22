(defproject libtxt "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/test.generative "0.5.1"]]
  :plugins [[lein-cljsbuild "1.0.2"]]
  :cljsbuild {:builds {:main {:source-paths ["src-cljs"]}}}
  :global-vars {*warn-on-reflection* true}
;  :aot [libtxt.jvm]
  :main libtxt.jvm)
