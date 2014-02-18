(ns txtlib.test.editor
  (:require [clojure.test :refer :all]
            [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [txtlib.core.editor :as editor]
            [txtlib.core.editor.notepad :as notepad]))

(defn editor []
  (gen/rand-nth [notepad/notepad]))

(defn modifier []
  (gen/rand-nth [:shift :ctrl :alt :meta]))

(defn input []
  (editor/->Input (gen/char) (gen/keyword) (gen/set modifier)))

(defspec run
  editor/run
  [^{:tag `editor} editor ^{:tag `input} input]
  (assert (and (editor/buffer %)
               (editor/history %)
               (editor/bounds %)
               (editor/clipboard %))))
