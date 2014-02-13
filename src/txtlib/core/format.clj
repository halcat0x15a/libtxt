(ns txtlib.core.format
  (:require [clojure.string :as string]
            [txtlib.core.buffer :as buffer]))

(defrecord Label [name value])

(defrecord Color [foreground background])

(defrecord Style [color cursor selection fontsize x y width height])

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

(defn view [string {:keys [x y width height]}]
  (->> (string/split string #"\n" -1)
       (drop y)
       (take height)
       (string/join \newline)))

(defn html
  ([node {:keys [color fontsize] :as style}]
     (str (format "<pre style=\"font-size:%spx;\">" fontsize)
          (view (html node style color) style)
          "</pre>"))
  ([{:keys [name value] :as node} style color]
     (cond (vector? node) (->> node (map #(html % style color)) string/join)
           (string? node) (span node color)
           (and name value) (html value style (get style name color)))))

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

(defn compute [{:keys [x y width height] :as bounds} buffer]
  (let [[a b] (buffer/position buffer)
        height (dec height)]
    (cond (< b y) (assoc bounds :y b)
          (> b (+ y height)) (assoc bounds :y (- b height))
          :else bounds)))
