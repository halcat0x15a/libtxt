(ns txtlib.core.buffer
  (:refer-clojure :exclude [char empty complement])
  (:require [txtlib.core :as core]
            [clojure.string :as string]))

(def complement
  {:left :right
   :right :left})

(def char
  {:left #"[\s\S]\z"
   :right #"\A[\s\S]"})

(def word
  {:left #"\w+\W*\z"
   :right #"\A\W*\w+"})

(def line
  {:left #"[^\n]*\z"
   :right #"\A[^\n]*"})

(defrecord Buffer [left right mark])

(defn buffer [string]
  (->Buffer "" string nil))

(def empty (buffer ""))

(defn show [{:keys [left right]}]
  (str left right))

(defn cursor [{:keys [left]}]
  (count left))

(defn- string-insert [string key value]
  (case key
    :left (str string value)
    :right (str value string)))

(defn insert [buffer key value]
  (update-in buffer [key] string-insert key value))

(defn- string-delete [string key n]
  (case key
    :left (subs string 0 (- (count string) n))
    :right (subs string n)))

(defn delete
  ([buffer n]
     (if (pos? n)
       (delete buffer :right n)
       (delete buffer :left (- n))))
  ([buffer key n]
     (update-in buffer [key] string-delete key n)))

(defn delete-matches [buffer key regex]
  (delete buffer key (count (re-find (key regex) (key buffer)))))

(defn move [buffer key regex]
  (if-let [result (re-find (key regex) (key buffer))]
    (-> buffer
        (delete key (count result))
        (insert (complement key) result))
    buffer))

(defn select [buffer]
  (assoc buffer :mark (cursor buffer)))

(defn deselect [buffer]
  (assoc buffer :mark nil))

(defn copy [{:keys [mark] :as buffer}]
  (if mark
    (apply subs (show buffer) (sort [(cursor buffer) mark]))))

(defn cut [{:keys [mark] :as buffer}]
  (if mark
    (delete buffer (- mark (cursor buffer)))
    buffer))
