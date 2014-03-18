(ns leiningen.libtxt
  (:refer-clojure :exclude [test])
  (:require [clojure.java.io :as io]
            [leiningen.core.eval :as eval]
            [leiningen.run :as run]
            [leiningen.test :as test]
            [leiningen.compile :as compile]
            [leiningen.cljsbuild :as cljsbuild])
  (:import [java.nio.file Path Paths Files CopyOption StandardCopyOption LinkOption DirectoryStream$Filter]
           [java.nio.file.attribute FileAttribute]))

(defn- directory? [path]
  (Files/isDirectory path (make-array LinkOption 0)))

(defn- path [first & more]
  (Paths/get first (into-array String more)))

(defn- paths [path]
  (if (directory? path)
    (mapcat paths (Files/newDirectoryStream path))
    [path]))

(defn- delete [path]
  (if (directory? path)
    (do
      (dorun (map delete (Files/newDirectoryStream path)))
      (Files/deleteIfExists path))
    (Files/deleteIfExists path)))

(defn- copy [src dst]
  (Files/copy src dst (make-array CopyOption 0)))

(defn- cljsbuild [{[clj] :source-paths
                   {{{[cljs] :source-paths} :main} :builds} :cljsbuild
                   :as project}
                  & args]
  (let [src (path clj "libtxt" "core")
        src-cljs (path cljs "libtxt" "core")
        core (.resolve (.getParent src-cljs) "core.cljs")]
    (delete src-cljs)
    (delete core)
    (doseq [clj (->> src paths (filter #(.endsWith (str %) "clj")))]
      (let [cljs (path (str (.resolve src-cljs (.relativize src clj)) \s))]
        (Files/createDirectories (.getParent cljs) (make-array FileAttribute 0))
        (copy clj cljs)))
    (copy (.resolveSibling src "core.clj") core)
    (apply cljsbuild/cljsbuild project args)))

(defn- test
  ([project] (test project "10000"))
  ([project msec]
     (eval/eval-in-project project
                           `(do
                              (System/setProperty "clojure.test.generative.msec" ~msec)
                              (runner/-main ~@(:test-paths project)))
                           '(require '[clojure.test.generative.runner :as runner]))
     (test/test project)))

(defn libtxt [project task & args]
  (apply (case task "test" test "cljsbuild" cljsbuild) project args))
