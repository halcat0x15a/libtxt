(ns txtlib.core.editor
  (:require [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]))

(defprotocol Editor
  (render [editor renderer])
  (run [editor input]))

(defn show [editor]
  (-> editor :current history/present buffer/show))

(defn insert [editor key value]
  (update-in editor [:current] history/edit buffer/insert key value))

(defn delete [editor key regex]
  (update-in editor [:current] history/edit buffer/delete-matches key regex))

(defn move [editor key regex]
  (update-in editor [:current] history/edit buffer/move key regex))
