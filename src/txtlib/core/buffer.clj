(ns txtlib.core.buffer
  (:refer-clojure :exclude [char chars keep empty complement])
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

(def all
  {:left #"[\s\S]*\z"
   :right #"\A[\s\S]*"})

(defrecord Buffer [left right mark])

(defn buffer [string]
  (->Buffer "" string nil))

(def empty (buffer ""))

(defn show [{:keys [left right]}]
  (str left right))

(defn cursor [{:keys [left]}]
  (count left))

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

(defn move [buffer key regex]
  (if-let [result (re-find (key regex) (key buffer))]
    (-> buffer
        (delete key (count result))
        (insert (complement key) result)
        (keep buffer))
    buffer))

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
