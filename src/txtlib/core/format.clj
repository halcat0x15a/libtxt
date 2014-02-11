(ns txtlib.core.format
  (:require [clojure.string :as string]
            [txtlib.core.buffer :as buffer]))

(defrecord Label [name value])

(defrecord Color [foreground background])

(def special
  {\< "&lt;"
   \> "&gt;"
   \& "&amp;"
   \newline (str \space \newline)})

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

(def normal "\\e[0m")

(def underline "\\e[4m")

(def negative "\\e[7m")

(defn show [{:keys [left right mark]}]
  (let [cursor (count left)
        right (str right \space)
        focus (str negative (first right) normal)
        right (subs right 1)]
    (cond (and mark (< mark cursor))
          (str (subs left 0 mark) underline (subs left mark) normal focus right)
          (and mark (> (dec mark) cursor))
          (let [r (dec (- mark cursor))]
            (str left focus underline (subs right 0 r) normal (subs right r)))
          :else (str left focus right))))
