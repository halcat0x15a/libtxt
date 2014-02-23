(ns txtlib.core.geometry
  (:require [clojure.string :as string]))

(defprotocol Shape
  (fix [shape point])
  (view [shape string]))

(defrecord Rectangle [x y width height]
  Shape
  (fix [rect [x' y']]
    (letfn [(fix [point' point length]
              (cond (< point' point) point'
                    (> point' (+ point length)) (- point' length)
                    :else point))]
      (assoc rect
        :x (fix x' x (dec width))
        :y (fix y' y (dec height)))))
  (view [rect string]
    (->> (concat (string/split string #"\n" -1) (repeat height ""))
         (drop y)
         (take height)
         (string/join \newline))))

(defn rect
  ([]
     (rect 0 0 0 0))
  ([x y width height]
     (Rectangle. x y width height)))
