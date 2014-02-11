(ns txtlib.core.format
  (:require [clojure.string :as string]
            [txtlib.core.buffer :as buffer]))

(def ^:dynamic *cursor* \u20de)
(def ^:dynamic *start* \u2192)
(def ^:dynamic *end* \u2190)

(defrecord Label [name value])

(defrecord Color [foreground background])

(def special
  {\< "&lt;"
   \> "&gt;"
   \& "&amp;"
   \newline " \n"})

(defn span [string {:keys [foreground background]}]
  (format "<span style=\"color:%s;background-color:%s;\">%s</span>"
          foreground
          background
          (string/escape string special)))

(defn html
  ([style node]
     (str "<pre>" (html (:default style) style node) "</pre>"))
  ([color style {:keys [name value] :as node}]
     (cond (vector? node) (->> node (map (partial html color style)) string/join)
           (string? node) (span node color)
           (and name value) (html (get style name color) style value))))

(defn show [{:keys [left right mark]}]
  (let [cursor (count left)
        right (str right \space)]
    (cond (and mark (< mark cursor))
          (str (subs left 0 mark) *start* (subs left mark) *end* *cursor* right)
          (and mark (> mark cursor))
          (let [r (- mark cursor)]
            (str left *start* *cursor* (subs right 0 r) *end* (subs right r)))
          :else (str left *cursor* right))))
