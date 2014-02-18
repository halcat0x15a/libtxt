(ns txtlib.core.map
  (:refer-clojure :exclude [vals remove])
  (:require [clojure.walk :as walk]))

(defrecord Map [key val keyvals])

(defn create [key val]
  (Map. key val {}))

(defn add [{:keys [key val keyvals] :as map} key' val']
  (assoc map
    :key key'
    :val val'
    :keyvals (if (= key key') keyvals (-> keyvals (dissoc key') (assoc key val)))))

(defn remove [{:keys [key keyvals] :as map} key']
  (if (= key key')
    (let [[key val] (first keyvals)]
      (assoc map
        :key key
        :val val
        :keyvals (apply hash-map (next keyvals))))
    (assoc map
      :keyvals (dissoc keyvals key'))))

(defn switch [{:keys [keyvals] :as map} key]
  (if-let [val (get keyvals key)]
    (-> map
        (remove key)
        (add key val))
    map))

(defn size [{:keys [keyvals]}]
  (inc (count keyvals)))

(defn vals [{:keys [val keyvals]}]
  (cons val (map second keyvals)))

(defn map-vals [{:keys [val keyvals] :as map} f & args]
  (assoc map
    :val (apply f val args)
    :keyvals (walk/walk (fn [[k v]] [k (apply f v args)]) identity keyvals)))

(defn reduce-vals [{:keys [val keyvals]} f]
  (->> keyvals
       (map second)
       (reduce f val)))
