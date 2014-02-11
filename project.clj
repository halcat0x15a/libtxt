(defproject txtlib "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/test.generative "0.5.1"]]
  :resource-paths [~(str (System/getProperty "java.home") "/lib/jfxrt.jar")]
  :plugins [[lein-cljsbuild "1.0.2"]]
  :cljsbuild {:builds []}
  :global-vars {*warn-on-reflection* true}
  :main txtlib.jvm)
