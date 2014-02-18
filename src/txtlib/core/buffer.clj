(ns txtlib.core.buffer
  (:require [clojure.string :as string]))

(defn- field [left right]
  {:left left
   :right right})

(def opposite (field :right :left))

(def character (field #"([\s\S])\z" #"\A([\s\S])"))

(def word (field #"(\w+\W*)\z" #"\A(\W*\w+)"))

(def characters (field #"(.*)\z" #"\A(.*)"))

(def line (field #"([\n\r].*)\z" #"\A(.*[\n\r])"))

(def all (field #"([\s\S]*)\z" #"\A([\s\S]*)"))

(defrecord Buffer [left right mark])

(defn buffer
  ([string]
     (buffer "" string))
  ([left right]
     (Buffer. left right nil)))

(def null (buffer ""))

(defn text [{:keys [left right]}]
  (str left right))

(defn cursor [{:keys [left]}]
  (count left))

(defn position [{:keys [left]}]
  [(count (second (re-find (:left characters) left)))
   (dec (count (string/split left #"\n" -1)))])

(defn mark [buffer]
  (assoc buffer :mark (cursor buffer)))

(defn activate [buffer]
  (if (:mark buffer)
    buffer
    (mark buffer)))

(defn deactivate [buffer]
  (assoc buffer :mark nil))

(defn- string-insert [string key value]
  (case key
    :left (str string value)
    :right (str value string)))

(defn insert [buffer key value]
  (-> buffer
      deactivate
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
         deactivate
         (update-in [key] string-delete key n))))

(defn regex-find [buffer key regex]
  (second (re-find (key regex) (key buffer))))

(defn delete-matches [buffer key regex]
  (if-let [result (regex-find buffer key regex)]
    (delete buffer key (count result))
    buffer))

(defn overwrite [buffer key value]
  (-> buffer
      (delete key (count value))
      (insert (opposite key) value)))

(defn move [{:keys [mark] :as buffer} key regex]
  (if-let [result (regex-find buffer key regex)]
    (-> buffer
        (overwrite key result)
        (assoc :mark mark))
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
