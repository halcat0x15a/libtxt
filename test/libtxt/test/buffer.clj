(ns libtxt.test.buffer
  (:refer-clojure :exclude [key])
  (:require [clojure.test :refer :all]
            [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [libtxt.core.buffer :as buffer]
            [libtxt.core.buffer.zipper :as zipper]
            [libtxt.jvm.buffer :as jvm]))

(defn key []
  (gen/rand-nth [buffer/left buffer/right]))

(defn zipper []
  (zipper/buffer (gen/string) (gen/string)))

(defn nio []
  (jvm/buffer (gen/string)))

(defn buffer []
  ((gen/rand-nth [zipper nio])))

(defspec insert-and-delete
  (fn [buffer key value]
    (delay (-> buffer (buffer/insert key value) (buffer/delete key (count value)))))
  [^{:tag `buffer} buffer ^{:tag `key} key ^string value]
  (assert (= (buffer/text buffer) (buffer/text @%))))

(defspec preserving-move
  (fn [buffer n]
    (delay (buffer/move buffer n)))
  [^{:tag `buffer} buffer ^int n]
  (assert (= (buffer/text buffer) (buffer/text @%))))

(defspec copy-and-paste
  (fn [buffer key n]
    (let [buffer (-> buffer buffer/mark (buffer/move key n))]
      (-> buffer buffer/cut (buffer/insert key (buffer/copy buffer)))))
  [^{:tag `buffer} buffer ^{:tag `key} key ^int n]
  (assert (= (buffer/text %) (buffer/text buffer))))
