(ns txtlib.core.editor
  (:refer-clojure :exclude [read newline])
  (:require [txtlib.core.lens :refer :all]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.frame :as frame]
            [txtlib.core.format :as format]))

(defprotocol Buffer
  (keymap [window]))

(defprotocol Editor
  (read [editor string])
  (render [editor format]))

(def frame (lens :frame))

(def current (compose frame/current frame))

(def buffer (compose (lens :buffer) current))

(def history (compose (lens :history) current))

(def bounds (compose (lens :bounds) current))

(def clipboard (lens :clipboard))

(defn path [editor]
  (-> editor frame :key))

(defn text [editor]
  (-> editor buffer buffer/text))

(defn changed? [editor]
  (-> editor buffer buffer/changed?))

(defn insert [editor key value]
  (update editor buffer buffer/insert key value))

(defn newline [editor]
  (insert editor :left \newline))

(defn delete [editor key regex]
  (update editor buffer buffer/delete-matches key regex))

(defn backspace [editor]
  (delete editor :left buffer/char))

(defn move [editor key regex]
  (update editor buffer buffer/move key regex))

(defn search [editor query]
  (update editor buffer buffer/search :right query))

(defn mark [editor]
  (update editor buffer buffer/mark))

(defn activate [editor]
  (update editor buffer buffer/activate))

(defn deactivate [editor]
  (update editor buffer buffer/deactivate))

(defn copy [editor]
  (if-let [string (-> editor buffer buffer/copy)]
    (update editor clipboard history/commit string)
    editor))

(defn cut [editor]
  (-> editor
      copy
      (update buffer buffer/cut)))

(defn paste [editor]
  (update editor buffer buffer/insert (-> editor clipboard history/present)))

(defn save [editor]
  (update editor buffer buffer/save))

(defn select-all [editor]
  (-> editor
      (move :left buffer/all)
      mark
      (move :right buffer/all)))

(defn undo [editor]
  (-> editor
      (buffer (-> editor history history/present))
      (update history history/undo)))

(defn commit [editor]
  (if (changed? editor)
    (update editor history history/commit (buffer editor))
    editor))

(defn add [editor key window]
  (update editor frame frame/add key window))

(defn open [editor key string]
  (add editor key (read editor string)))

(defn switch [editor key]
  (update editor frame frame/switch key))

(defn compute [editor]
  (update editor bounds format/compute (buffer editor)))

(defrecord Input [char key modifiers])

(defn input
  ([char key modifiers]
     (Input. char key modifiers))
  ([char key shift? ctrl? alt? meta?]
     (->> {:shift shift? :ctrl ctrl? :alt alt? :meta meta?}
          (filter second)
          (map first)
          set
          (Input. char key))))

(defn run [editor {:keys [char key modifiers] :as input}]
  (let [{:keys [run] :as keymap} (merge (:keymap editor) (-> editor current keymap))]
    (if-let [run (or (get keymap (conj modifiers key)) (get keymap char))]
      (-> editor run compute)
      (-> editor (run input) compute))))
