(ns txtlib.core.editor
  (:require [txtlib.core.lens :refer :all]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.format :as format]))

(defprotocol Editor
  (buffer [editor] [editor buffer])
  (history [editor] [editor history])
  (bounds [editor] [editor bounds]))

(defn text [editor]
  (-> editor buffer buffer/text))

(defn changed? [editor]
  (-> editor buffer buffer/changed?))

(defn insert [editor key value]
  (update editor buffer buffer/insert key value))

(defn delete [editor key regex]
  (update editor buffer buffer/delete-matches key regex))

(defn move [editor key regex]
  (update editor buffer buffer/move key regex))

(defn mark [editor]
  (update editor buffer buffer/mark))

(defn activate [editor]
  (update editor buffer buffer/activate))

(defn deactivate [editor]
  (update editor buffer buffer/deactivate))

(defn save [editor]
  (update editor buffer buffer/save))

(defn select-all [editor]
  (-> editor
      (move :left buffer/all)
      mark
      (move :right buffer/all)))

(defn undo [editor]
  (update editor history history/undo))

(defn commit [editor]
  (if (changed? editor)
    (update editor history history/commit (buffer editor))
    editor))

(defn compute [editor]
  (update editor bounds format/compute (buffer editor)))
