(ns libtxt.core.canvas
  (:require [clojure.string :as string]))

(defprotocol Node
  (render [node canvas]))

(defn canvas [source x y width height]
  (->> (string/split-lines source)
       (drop y)
       (take height)
       (map #(subs % x (min width (count %))))))
;  (to-array-2d (repeat height (repeat width nil))))
