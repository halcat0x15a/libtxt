(ns libtxt.core.history
  (:require [clojure.zip :as zip]))

(defn root [value]
  (zip/down (zip/vector-zip [value])))

(defn undo [history]
  (or (some-> history zip/up zip/right) history))

(defn redo [history]
  (or (some-> history zip/left zip/down) history))

(defn commit [history value]
  (-> history
      (zip/insert-right (zip/node history))
      (zip/replace [])
      (zip/insert-child value)
      zip/down))
