(ns txtlib.core.history
  (:refer-clojure :exclude [future])
  (:require [clojure.zip :as zip]))

(defprotocol History
  (value [history])
  (future [history] [history future]))

(deftype Change [value future]
  History
  (value [history] value)
  (future [history] future)
  (future [history future] (Change. value future)))

(defn change [value]
  (Change. value nil))

(defn history [value]
  (zip/zipper future future future (change value)))

(defn present [history]
  (-> history zip/node value))

(defn undo [history]
  (or (zip/up history) history))

(defn redo [history]
  (or (zip/down history) history))

(defn commit [history value]
  (-> history
      (zip/edit future [])
      (zip/insert-child (change value))
      zip/down))
