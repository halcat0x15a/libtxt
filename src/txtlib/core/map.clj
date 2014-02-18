(ns txtlib.core.map
  (:require [clojure.walk :as walk]))

(defrecord Map [key val keyvals])

(defn create [key val]
  (Map. key val {}))

(defn add [{:keys [key val keyvals] :as map} key' val']
  (assoc map
    :key key'
    :val val'
    :keyvals (if (= key key') keyvals (-> keyvals (dissoc key') (assoc key val)))))

(defn delete [{:keys [key keyvals] :as map} key']
  (if (= key key')
    (let [[key val] (first keyvals)]
      (assoc map
        :key key
        :val val
        :keyvals (into {} (next keyvals))))
    (assoc map
      :keyvals (dissoc keyvals key'))))

(defn switch [{:keys [keyvals] :as map} key]
  (if-let [val (get keyvals key)]
    (-> map
        (delete key)
        (add key val))
    map))

(defn size [{:keys [keyvals]}]
  (inc (count keyvals)))

(defn values [{:keys [val keyvals]}]
  (cons val (map second keyvals)))

(defn map-values [{:keys [val keyvals] :as map} f & args]
  (assoc map
    :val (apply f val args)
    :keyvals (walk/walk (fn [[k v]] [k (apply f v args)]) identity keyvals)))

(defn reduce-values [{:keys [val keyvals]} f]
  (->> keyvals
       (map second)
       (reduce f val)))

(defn next-key [{:keys [keyvals]}]
  (some-> keyvals last key))
