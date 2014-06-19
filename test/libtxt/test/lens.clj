(ns libtxt.test.lens
  (:refer-clojure :exclude [keyword])
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [libtxt.core.lens :as lens]))

(defn keyword []
  (let [key (gen/keyword)]
    [(lens/lens key) {key (gen/anything)}]))

(defn lens []
  ((gen/rand-nth [keyword])))

(defspec get-set
  (fn [[lens object]]
    (lens object (lens object)))
  [^{:tag `lens} [lens object]]
  (assert (= % object)))
