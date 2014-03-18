(ns libtxt.core.html
  (:require [clojure.string :as string]))

(declare html)

(defprotocol Html
  (show [html]))

(defn attribute [key val]
  (str key "=\"" val "\""))

(defn attributes [keyvals]
  (->> keyvals (map (partial apply attribute)) (interleave (repeat \space)) string/join))

(defn element [tag value keyvals]
  (str "<" tag (attributes keyvals) ">" (html value) "</" tag ">"))

(deftype Element [tag value attributes]
  Html
  (show [this]
    (element tag value attributes)))

(defn html [node]
  (cond (vector? node) (string/join (map html node))
        (string? node) node
        (satisfies? Html node) (show node)))
