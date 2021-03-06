(ns txtlib.core.history
  (:require [clojure.zip :as zip]))

(defrecord Change [present future])

(defn change [value] (Change. value nil))

(defn- make-node [change children]
  (assoc change :future children))

(defn history [value]
  (zip/zipper :future :future make-node (change value)))

(defn present [history]
  (-> history zip/node :present))

(def undo zip/up)

(def redo zip/down)

(defn commit [history value]
  (-> history
      (zip/edit assoc :future [])
      (zip/insert-child (change value))
      zip/down))

(defn edit [history f & args]
  (apply zip/edit history update-in [:present] f args))
