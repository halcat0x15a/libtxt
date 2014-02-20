(ns leiningen.txtlib
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

(defn- cljsbuild [{[clj] :source-paths
                   {{{[cljs] :source-paths} :main} :builds} :cljsbuild
                   :as project}
                  & args]
  (let [src (path clj "txtlib" "core")
        src-cljs (path cljs "txtlib" "core")]
    (delete src-cljs)
    (doseq [clj (->> src paths (filter #(.endsWith (str %) "clj")))]
      (let [cljs (path (str (.resolve src-cljs (.relativize src clj)) \s))]
        (Files/createDirectories (.getParent cljs) (make-array FileAttribute 0))
        (Files/copy clj cljs (make-array CopyOption 0))))
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

(defn txtlib [project task & args]
  (apply (case task "test" test "cljsbuild" cljsbuild) project args))
