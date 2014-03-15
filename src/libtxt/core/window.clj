(ns libtxt.core.window
  (:require [clojure.zip :as zip]))

(defn frame [& windows]
  (zip/vector-zip (vec windows)))
