(ns txtlib.test.buffer
  (:refer-clojure :exclude [key])
  (:require [clojure.test :refer :all]
            [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [txtlib.core.buffer :as buffer]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string) nil))

(defn key []
  (gen/rand-nth [:left :right]))

(defn regex []
  (gen/rand-nth [buffer/char buffer/line buffer/word]))

(defspec double-complement
  (comp buffer/complement buffer/complement)
  [^{:tag `key} key]
  (assert (= % key)))

(defspec preserving-move
  buffer/move
  [^{:tag `buffer} buffer ^{:tag `key} key ^{:tag `regex} regex]
  (assert (= (buffer/text %) (buffer/text buffer))))

(defspec copy-and-paste
  (fn [buffer key regex]
    (let [buffer (-> buffer buffer/mark (buffer/move key regex))]
      (-> buffer buffer/cut (buffer/insert key (buffer/copy buffer)))))
  [^{:tag `buffer} buffer ^{:tag `key} key ^{:tag `regex} regex]
  (assert (= (buffer/text %) (buffer/text buffer))))
