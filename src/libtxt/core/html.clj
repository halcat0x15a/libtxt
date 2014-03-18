(ns libtxt.core.html
  (:require [clojure.string :as string]))

(declare html)

(defn attribute [name value]
  (str name "='" value "'"))

(defn element [tag value & attributes]
  (str "<" tag (->> attributes (partition 2) (map (partial apply attribute)) (interleave (repeat \space)) string/join) ">" (html value) "</" tag ">"))

(defmulti label->element (fn [{:keys [name value]}] name))
(defmethod label->element :header [{:keys [value] :as label}]
  (element (str "h" (:rank (meta label))) value))
(defmethod label->element :anchor [{:keys [value] :as label}]
  (element "a" value "href" (:url (meta label))))
(defmethod label->element :list-item [{:keys [value]}]
  (element "li" value))
(defmethod label->element :unordered-list [{:keys [value]}]
  (element "ul" value))
(defmethod label->element :paragraph [{:keys [value]}]
  (element "p" value))

(defn html [{:keys [name value] :as node}]
  (cond (vector? node) (string/join (map html node))
        (string? node) node
        (and name value) (label->element node)))
