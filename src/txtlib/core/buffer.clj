(ns txtlib.core.buffer
  (:refer-clojure :exclude [char chars empty complement])
  (:require [clojure.string :as string]))

(def complement
  {:left :right
   :right :left})

(def char
  {:left #"[\s\S]\z"
   :right #"\A[\s\S]"})

(def word
  {:left #"\w+\W*\z"
   :right #"\A\W*\w+"})

(def chars
  {:left #"[^\n]*\z"
   :right #"\A[^\n]*"})

(def line
  {:left #"\n[^\n]*\z"
   :right #"\A[^\n]*\n"})

(defrecord Buffer [left right mark])

(defn buffer [string]
  (->Buffer "" string nil))

(def empty (buffer ""))

(defn show
  ([buffer]
     (show buffer ""))
  ([{:keys [left right]} sep]
     (str left sep right)))

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

(defn selection [{:keys [mark] :as buffer}]
  (if mark
    (sort [(cursor buffer) mark])))

(defn copy [buffer]
  (if-let [selection (selection buffer)]
    (apply subs (show buffer) selection)))

(defn cut [{:keys [mark] :as buffer}]
  (if mark
    (delete buffer (- mark (cursor buffer)))
    buffer))
