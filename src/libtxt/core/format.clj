(ns libtxt.core.format
  (:require [clojure.string :as string]
            [libtxt.core.buffer :as buffer]))

(defrecord Label [name value])

(defrecord Color [foreground background])

(def *style* ^:dynamic
  {:color (Color. "black" "white")
   :cursor (Color. "white" "black")
   :selection (Color. "white" "gray")})

(def special
  {\< "&lt;"
   \> "&gt;"
   \& "&amp;"
   \newline (str \space \newline)})

(defn html
  ([string foreground background]
     (str "<span style=\"color:" foreground ";background-color:" background ";\">"
          ;(string/escape string special)
          string
          "</span>"))
  ([string fontsize]
     (str "<pre style=\"font-size:" fontsize "px;\">"
          string
          "</pre>")))

#_(defn render [{:keys [name value] :as node} format style {:keys [foreground background] :as color}]
  (cond (vector? node) (->> node (map #(render % format style color)) string/join)
        (string? node) (format node foreground background)
        (and name value) (render value format style (get style name color))))

(defn render [source format style]
  (let [source (transient (vec (str source \space)))]
    (->> style
         (reduce (fn [_ [index {:keys [foreground background]}]]
                   (prn index)
                   (assoc! source index (format (nth source index) foreground background)))
                 style)
         persistent!
         string/join)))

(defn buffer [buffer]
  (assoc (if-let [[start end] (buffer/selection buffer)]
           (zipmap (range start end) (repeat :selection)))
    (buffer/cursor buffer) :cursor))

#_(defn buffer [buffer]
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

(render "hello" html (buffer (libtxt.core.buffer/buffer "hello")))
