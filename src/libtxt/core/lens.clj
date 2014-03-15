(ns libtxt.core.lens
  (:require [clojure.zip :as zip]))

(defn lens [key]
  (fn
    ([object]
       (get object key))
    ([object value]
       (assoc object key value))))

(defn update [object lens f & args]
  (lens object (apply f (lens object) args)))

(defn compose
  ([f g]
     (fn
       ([object]
          (f (g object)))
       ([object value]
          (g object (f (g object) value)))))
  ([f g & h]
     (reduce compose (compose f g) h)))

(def zipper
  (fn
    ([zipper]
       (zip/node zipper))
    ([zipper value]
       (zip/replace zipper value))))
