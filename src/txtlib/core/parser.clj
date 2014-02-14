(ns txtlib.core.parser
  (:refer-clojure :exclude [not map]))

(defprotocol Functor
  (map [m f]))

(defrecord Success [value next]
  Functor
  (map [success f] (Success. (f value) next)))

(defrecord Failure [next]
  Functor
  (map [failure f] failure))

(defn- extract [x]
  (cond (string? x) x
        (vector? x) (first x)))

(defn regex
  ([pattern] (partial regex pattern))
  ([pattern input]
     (if-let [result (->> input (re-find pattern) extract)]
       (Success. result (subs input (count result)))
       (Failure. input))))

(defn string
  ([pattern] (partial string pattern))
  ([pattern input]
     (let [length (count pattern)]
       (cond (< (count input) length) (Failure. input)
             (= (subs input 0 length) pattern) (Success. pattern (subs input length))
             :else (Failure. input)))))

(defn many [parser]
  (fn [input]
    (loop [result [] input input]
      (let [{:keys [value next]} (parser input)]
        (if (= input next)
          (Success. result input)
          (recur (conj result value) next))))))

(defn not [parser]
  (fn [input]
    (let [{:keys [value] :as result} (parser input)]
      (if value
        (Failure. input)
        (Success. nil input)))))

(defn choice [parser & parsers]
  (fn [input]
    (loop [[parser & parsers] (cons parser parsers)]
      (let [{:keys [value] :as result} (parser input)]
        (if (or value (empty? parsers))
          result
          (recur parsers))))))

(defn chain [f parser & parsers]
  (fn [input]
    (loop [{:keys [value next] :as result} (map (parser input) (partial partial f))
           [parser & parsers] parsers]
      (cond (and value (nil? parser)) (map result #(%))
            value (recur (map (parser next) (partial partial value)) parsers)
            :else (Failure. input)))))

(defn one [input]
  (if-not (empty? input)
    (Success. (subs input 0 1) (subs input 1))
    (Failure. input)))
