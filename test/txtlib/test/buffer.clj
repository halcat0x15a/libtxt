(ns txtlib.test.buffer
  (:refer-clojure :exclude [key])
  (:require [clojure.test :refer :all]
            [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [txtlib.core.buffer :as buffer]))

(defn buffer []
  (buffer/buffer (gen/string) (gen/string)))

(defn key []
  (gen/rand-nth [:left :right]))

(defn regex []
  (gen/rand-nth [buffer/character buffer/line buffer/word buffer/characters buffer/all]))

(defspec double-opposite
  (comp buffer/opposite buffer/opposite)
  [^{:tag `key} key]
  (assert (= % key)))

(defspec preserving-move
  (fn [buffer key regex]
    (buffer/matches buffer buffer/move key regex))
  [^{:tag `buffer} buffer ^{:tag `key} key ^{:tag `regex} regex]
  (assert (= (buffer/text %) (buffer/text buffer))))

(defspec copy-and-paste
  (fn [buffer key regex]
    (let [buffer (-> buffer buffer/mark (buffer/matches buffer/move key regex))]
      (-> buffer buffer/cut (buffer/insert key (buffer/copy buffer)))))
  [^{:tag `buffer} buffer ^{:tag `key} key ^{:tag `regex} regex]
  (assert (= (buffer/text %) (buffer/text buffer))))
