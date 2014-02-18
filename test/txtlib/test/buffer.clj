(ns txtlib.test.buffer
  (:require [clojure.test :refer :all]
            [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [txtlib.core.buffer :as buffer]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string) nil))

(defn field []
  (gen/rand-nth [:left :right]))

(defn regex []
  (gen/rand-nth [buffer/character buffer/line buffer/word buffer/characters buffer/all]))

(defspec double-opposite
  (comp buffer/opposite buffer/opposite)
  [^{:tag `field} field]
  (assert (= % field)))

(defspec preserving-move
  buffer/move
  [^{:tag `buffer} buffer ^{:tag `field} field ^{:tag `regex} regex]
  (assert (= (buffer/text %) (buffer/text buffer))))

(defspec copy-and-paste
  (fn [buffer field regex]
    (let [buffer (-> buffer buffer/mark (buffer/move field regex))]
      (-> buffer buffer/cut (buffer/insert field (buffer/copy buffer)))))
  [^{:tag `buffer} buffer ^{:tag `field} field ^{:tag `regex} regex]
  (assert (= (buffer/text %) (buffer/text buffer))))
