(ns txtlib.core.format
  (:require [clojure.string :as string]
            [txtlib.core.buffer :as buffer]))

(defrecord Label [name value])

(defrecord Color [foreground background])

(defrecord Rectangle [x y width height])

(defn rectangle
  ([]
     (Rectangle. 0 0 0 0))
  ([x y width height]
     (Rectangle. x y width height)))

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

(defn pre [string {:keys [fontsize]}]
  (format "<pre style=\"font-size:%spx;\">%s</pre>" fontsize string))

(defn render
  ([node format {:keys [color] :as style}]
     (render node format style color))
  ([{:keys [name value] :as node} format style color]
     (cond (vector? node) (->> node (map #(render % format style color)) string/join)
           (string? node) (format node color)
           (and name value) (render value format style (get style name color)))))

(defn view [string {:keys [x y width height]}]
  (->> (string/split string #"\n" -1)
       (drop y)
       (take height)
       (string/join \newline)))

(defn compute [{:keys [x y width height] :as bounds} buffer]
  (let [[a b] (buffer/position buffer)
        height (dec height)]
    (cond (< b y) (assoc bounds :y b)
          (> b (+ y height)) (assoc bounds :y (- b height))
          :else bounds)))

(defn buffer [buffer]
  (let [text (str (buffer/text buffer) \space)
        cursor (buffer/cursor buffer)
        [start end :as selection] (buffer/selection buffer)]
    (cond (and selection (= end cursor))
          [(subs text 0 start)
           (Label. :selection (subs text start end))
           (Label. :cursor (subs text end (inc end)))
           (subs text (inc end))]
          (and selection (= start cursor))
          [(subs text 0 start)
           (Label. :cursor (subs text start (inc start)))
           (Label. :selection (subs text (inc start) end))
           (subs text end)]
          :else
          [(subs text 0 cursor)
           (Label. :cursor (subs text cursor (inc cursor)))
           (subs text (inc cursor))])))
