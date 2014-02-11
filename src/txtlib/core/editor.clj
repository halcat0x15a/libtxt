(ns txtlib.core.editor
  (:require [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]))

(defprotocol Editor
  (buffer [editor] [editor buffer])
  (render [editor renderer])
  (run [editor input]))

(defprotocol Clipboard
  (clipboard [editor] [editor buffer]))

(defrecord Input [char key modifiers])

(defn update [editor lens f & args]
  (lens editor (apply f (lens editor) args)))

(defn input
  ([char key modifiers]
     (Input. char key modifiers))
  ([char key shift? ctrl? alt?]
     (->> {:shift shift? :ctrl ctrl? :alt alt?}
          (filter second)
          (map first)
          set
          (Input. char key))))

(defn show [editor]
  (-> editor buffer history/present buffer/show))

(defn insert [editor key value]
  (update editor buffer history/edit buffer/insert key value))

(defn delete [editor key regex]
  (update editor buffer history/edit buffer/delete-matches key regex))

(defn move [editor key regex]
  (update editor buffer history/edit buffer/move key regex))

(defn mark [editor]
  (update editor buffer history/edit buffer/mark))

(defn activate [editor]
  (update editor buffer history/edit buffer/activate))

(defn deactivate [editor]
  (update editor buffer history/edit buffer/deactivate))

(defn copy [editor]
  (if-let [string (-> editor buffer history/present buffer/copy)]
    (update editor clipboard history/commit string)
    editor))

(defn cut [editor]
  (-> editor
      copy
      (update buffer history/edit buffer/cut)))

(defn paste [editor]
  (insert editor :left (-> editor clipboard history/present)))

(defn undo [editor]
  (update editor buffer history/undo))

(defn commit [editor]
  (update editor buffer history/commit (-> editor buffer history/present)))

(defn changed? [editor]
  (-> editor buffer history/present buffer/changed?))

(defn open [editor path string]
  (buffer editor (vary-meta (history/history (buffer/buffer string)) assoc ::path path)))

(defn save [editor]
  (update editor buffer history/edit buffer/save))

(defn path [editor]
  (-> editor buffer meta ::path))
