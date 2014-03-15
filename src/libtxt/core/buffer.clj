(ns libtxt.core.buffer
  (:refer-clojure :exclude [empty])
  (:require [clojure.string :as string]))

(defn- pair [left right]
  #(case %
     :left left
     :right right))

(def opposite (pair :right :left))

(def character (pair #"([\s\S])$" #"^([\s\S])"))

(def word (pair #"(\w+\W*)$" #"^(\W*\w+)"))

(def characters (pair #"(.*)$" #"^(.*)"))

(def line (pair #"([\n\r].*)$" #"^(.*[\n\r])"))

(def all (pair #"([\s\S]*)$" #"^([\s\S]*)"))

(defprotocol Buffer
  (text [buffer])
  (cursor [buffer])
  (position [buffer]))

(defrecord Zipper [left right mark]
  Buffer
  (text [buffer]
    (str left right))
  (cursor [buffer]
    (count left))
  (position [buffer]
    [(count (second (re-find (characters :left) left)))
     (dec (count (string/split left #"\n" -1)))]))

(defn buffer
  ([string]
     (buffer "" string))
  ([left right]
     (Zipper. left right nil)))

(def empty (buffer ""))

(defn mark [buffer]
  (assoc buffer :mark (cursor buffer)))

(defn activate [buffer]
  (if (:mark buffer)
    buffer
    (mark buffer)))

(defn deactivate [buffer]
  (assoc buffer :mark nil))

(defn insert [buffer key value]
  (let [string (key buffer)]
    (-> buffer
        deactivate
        (assoc key (case key :left (str string value) :right (str value string))))))

(defn delete
  ([buffer n]
     (if (pos? n)
       (delete buffer :right n)
       (delete buffer :left (- n))))
  ([buffer key n]
     (let [string (key buffer)
           length (count string)]
       (if (<= 1 n length)
         (-> buffer
             deactivate
             (assoc key (case key :left (subs string 0 (- length n)) :right (subs string n))))
         buffer))))

(defn move [buffer key n]
  (let [string (key buffer)
        length (count string)]
    (if (<= 1 n length)
      (-> buffer
          (delete key n)
          (insert (opposite key) (case key :left (subs string (- length n)) :right (subs string 0 n)))
          (assoc :mark (:mark buffer)))
      buffer)))

(defn matches [buffer f key regex]
  (if-let [result (second (re-find (regex key) (key buffer)))]
    (f buffer key (count result))
    buffer))

(defn selection [buffer]
  (if-let [mark (:mark buffer)]
    (sort [(cursor buffer) mark])))

(defn copy [buffer]
  (if-let [selection (selection buffer)]
    (apply subs (text buffer) selection)))

(defn cut [buffer]
  (if-let [mark (:mark buffer)]
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
