(ns libtxt.core.editor.slide
  (:require [clojure.zip :as zip]
            [clojure.string :as string]
            [libtxt.core.lens :refer [lens update compose]]
            [libtxt.core.parser :as parser]
            [libtxt.core.markdown :as md]
            [libtxt.core.html :as html]
            [libtxt.core.buffer :as buffer]
            [libtxt.core.editor :as editor]))

(def ^String delimiter "!SLIDE")

(def slide (compose (lens :slide) editor/current))

(defn split [^String string]
  (loop [result []
         string string]
    (let [n (.indexOf string delimiter)]
      (if (neg? n)
        (conj result string)
        (recur (conj result (subs string 0 n)) (subs string (+ n (count delimiter))))))))

(defn slides [string]
  (->> string
       split
       (map (comp html/html :value md/markdown))
       (filter (complement empty?))
       zip/seq-zip))

(defn move [editor f]
  (if-let [slides (f (slide editor))]
    (-> editor (slide slides) (editor/buffer (buffer/buffer (zip/node slides))))
    editor))

(def right #(or (zip/right %) %))

(def view
  {#{:left} #(move % zip/left)
   #{:right} #(move % zip/right)
   :default (fn [editor _] editor)})

(defrecord Slide [buffer history keymap slide])

(defn start [editor]
  (-> editor
      (update editor/current #(map->Slide (assoc % :slide (slides (buffer/text (editor/buffer editor))))))
      (update editor/keymap #(merge % view))
      (move zip/down)))
