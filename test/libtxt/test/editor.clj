(ns libtxt.test.editor
  (:require [clojure.test :refer :all]
            [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [libtxt.core.editor :as editor]
            [libtxt.core.editor.notepad :as notepad]))

(defn editor []
  (gen/rand-nth [notepad/notepad]))

(defn modifier []
  (gen/rand-nth [:shift :ctrl :alt :meta]))

(defn event []
  (editor/event (gen/char) (gen/keyword) (gen/set modifier)))

#_(defspec run
  editor/run
  [^{:tag `editor} editor ^{:tag `event} event]
  (assert (and (editor/buffer %)
               (editor/history %)
               (editor/id %)
               (editor/bounds %)
               (editor/keymap %)
               (editor/clipboard %)
               (editor/width %)
               (editor/height %))))
