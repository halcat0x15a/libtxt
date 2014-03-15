(ns libtxt.core
  (:require [clojure.walk :as walk]))

(defn map-values [f map]
  (walk/walk (fn [[k v]] [k (f v)]) identity map))

(defn map-keys [f map]
  (walk/walk (fn [[k v]] [(f k) v]) identity map))
