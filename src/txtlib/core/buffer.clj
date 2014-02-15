(ns txtlib.core.buffer
  (:refer-clojure :exclude [char chars keep empty complement])
  (:require [clojure.string :as string]))

(def complement
  {:left :right
   :right :left})

(def char
  {:left #"([\s\S])\z"
   :right #"\A([\s\S])"})

(def word
  {:left #"(\w+\W*)\z"
   :right #"\A(\W*\w+)"})

(def chars
  {:left #"([^\n\r]*)\z"
   :right #"\A([^\n\r]*)"})

(def line
  {:left #"([\n\r][^\n\r]*)\z"
   :right #"\A([^\n\r]*[\n\r])"})

(def all
  {:left #"([\s\S]*)\z"
   :right #"\A([\s\S]*)"})

(defrecord Buffer [left right mark])

(defn buffer [string]
  (->Buffer "" string nil))

(def empty (buffer ""))

(defn text [{:keys [left right]}]
  (str left right))

(defn cursor [{:keys [left]}]
  (count left))

(defn position [{:keys [left]}]
  [(count (re-find #"[^\n]*\z" left))
   (dec (count (string/split left #"\n" -1)))])

(defn mark [buffer]
  (assoc buffer :mark (cursor buffer)))

(defn activate [buffer]
  (if (:mark buffer)
    buffer
    (mark buffer)))

(defn deactivate [buffer]
  (assoc buffer :mark nil))

(defn changed? [buffer]
  (-> buffer
      meta
      ::changed?))

(defn touch [buffer]
  (-> buffer
      (vary-meta assoc ::changed? true)
      deactivate))

(defn save [buffer]
  (vary-meta buffer dissoc ::changed?))

(defn keep [buffer previous]
  (-> buffer
      (assoc :mark (:mark previous))
      (with-meta (meta previous))))

(defn- string-insert [string key value]
  (case key
    :left (str string value)
    :right (str value string)))

(defn insert [buffer key value]
  (-> buffer
      touch
      (update-in [key] string-insert key value)))

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
     (-> buffer
         touch
         (update-in [key] string-delete key n))))

(defn delete-matches [buffer key regex]
  (delete buffer key (count (re-find (key regex) (key buffer)))))

(defn overwrite [buffer key value]
  (-> buffer
      (delete key (count value))
      (insert (complement key) value)))

(defn move [buffer key regex]
  (if-let [[_ result] (re-find (key regex) (key buffer))]
    (-> buffer
        (overwrite key result)
        (keep buffer))
    buffer))

(defn selection [{:keys [mark] :as buffer}]
  (if mark
    (sort [(cursor buffer) mark])))

(defn copy [buffer]
  (if-let [selection (selection buffer)]
    (apply subs (text buffer) selection)))

(defn cut [{:keys [mark] :as buffer}]
  (if mark
    (delete buffer (- mark (cursor buffer)))
    buffer))

(defn search [buffer key pattern]
  (let [pattern (string/re-quote-replacement pattern)
        regex {:left (re-pattern (str pattern "([\\s\\S]*?)\\z"))
               :right (re-pattern (str "\\A([\\s\\S]*?)" pattern))}]
    (move buffer key regex)))
