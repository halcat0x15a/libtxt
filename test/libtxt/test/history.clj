(ns libtxt.test.history
  (:require [clojure.zip :as zip]
            [clojure.data.generators :as gen]
            [clojure.test.generative :refer :all]
            [libtxt.core.history :as history]))

(defn history []
  (history/root (gen/anything)))

(defspec undo
  (fn [history value]
    (-> history
        (history/commit value)
        history/undo))
  [^{:tag `history} history ^anything value]
  (assert (= (zip/node %) (zip/node history))))
