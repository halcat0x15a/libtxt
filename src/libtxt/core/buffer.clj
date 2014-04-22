(ns libtxt.core.buffer
  (:refer-clojure :exclude [empty])
  (:require [clojure.string :as string]
            [libtxt.core.buffer.protocol :as protocol]
            [libtxt.core.buffer.zipper :as zipper]))

(def ^:dynamic *buffer* zipper/buffer)

(defn buffer
  ([]
     (buffer ""))
  ([value]
     (*buffer* value)))

(defn- pair [left right]
  (reify protocol/Zipper
    (left [zipper] left)
    (right [zipper] right)))

(defn sig [n]
  (reify protocol/Zipper
    (left [zipper] (- n))
    (right [zipper] n)))

(def left protocol/left)

(def right protocol/right)

(def opposite (pair right left))

(def character (pair #"([\s\S])$" #"^([\s\S])"))

(def word (pair #"(\w+\W*)$" #"^(\W*\w+)"))

(def characters (pair #"(.*)$" #"^(.*)"))

(def line (pair #"([\n\r].*)$" #"^(.*[\n\r])"))

(def all (pair #"([\s\S]*)$" #"^([\s\S]*)"))

(defn text [buffer]
  (str (left buffer) (right buffer)))

(defn cursor [buffer]
  (count (left buffer)))

(defn length [buffer]
  (count (text buffer)))

(defn mark [buffer]
  (vary-meta buffer assoc :mark (cursor buffer)))

(defn activate [buffer]
  (if (:mark (meta buffer))
    buffer
    (mark buffer)))

(defn deactivate [buffer]
  (vary-meta buffer assoc :mark nil))

(defn insert [buffer key value]
  (-> (reify protocol/Zipper
        (left [zipper] (protocol/insert-left buffer value))
        (right [zipper] (protocol/insert-right buffer value)))
      key
      deactivate))

(defn delete
  ([buffer key n]
     (delete buffer (key (sig n))))
  ([buffer n]
     (cond-> buffer
       (<= 0 (+ (cursor buffer) n) (length buffer))
       (-> (protocol/delete n) deactivate))))

(defn move
  ([buffer key n]
     (move buffer (key (sig n))))
  ([buffer n]
     (cond-> buffer
       (<= 0 (+ (cursor buffer) n) (length buffer))
       (protocol/move n))))

(defn matches [buffer f key regex]
  (if-let [result (second (re-find (key regex) (key buffer)))]
    (f buffer (key (sig (count result))))
    buffer))

(defn selection [buffer]
  (if-let [mark (:mark (meta buffer))]
    (let [cursor (cursor buffer)]
      [(min cursor mark) (max cursor mark)])))

(defn copy [buffer]
  (if-let [selection (selection buffer)]
    (apply subs (text buffer) selection)))

(defn cut [buffer]
  (if-let [mark (:mark (meta buffer))]
    (delete buffer (- mark (cursor buffer)))
    buffer))

(defn search [buffer key ^String query]
  (let [^String string (key buffer)
        n (case key
            :left (.lastIndexOf string query)
            :right (.indexOf string query))]
    (if (pos? n)
      (move buffer key n)
      buffer)))
