(ns leiningen.txtlib
  (:require [clojure.java.io :as io]
            [leiningen.core.eval :as eval]
            [leiningen.run :as run]
            [leiningen.test :as test]
            [leiningen.compile :as compile]
            [leiningen.cljsbuild :as cljsbuild])
  (:import [java.nio.file Path Paths Files CopyOption StandardCopyOption LinkOption DirectoryStream$Filter]
           [java.nio.file.attribute FileAttribute]))

(def link-option (make-array LinkOption 0))

(defn- path [first & more]
  (Paths/get first (into-array String more)))

(defn- paths [path]
  (if (Files/isDirectory path link-option)
    (mapcat paths (Files/newDirectoryStream path))
    [path]))

(defn- delete [path]
  (if (Files/isDirectory path link-option)
    (do
      (dorun (map delete (Files/newDirectoryStream path)))
      (Files/delete path))
    (Files/delete path)))

(defn- cljsbuild [{[clj] :source-paths
                   {{{[cljs] :source-paths} :main} :builds} :cljsbuild
                   :as project}]
  (let [src (path clj "txtlib" "core")
        src-cljs (path cljs "txtlib" "core")]
    (delete src-cljs)
    (doseq [clj (->> src paths (filter #(.endsWith (str %) "clj")))]
      (let [cljs (path (str (.resolve src-cljs (.relativize src clj)) \s))]
        (Files/createDirectories (.getParent cljs) (make-array FileAttribute 0))
        (Files/copy clj cljs (make-array CopyOption 0))))
    (cljsbuild/cljsbuild project "auto")))

(defn txtlib [project task]
  (case task
    "run" (do
            (compile/compile project "txtlib.jvm")
            (run/run project))
    "test" (do
             (eval/eval-in-project project
                                   `(clojure.test.generative.runner/-main "test")
                                   '(require 'clojure.test.generative.runner))
             (test/test project))
    "compile" (cljsbuild project)))
