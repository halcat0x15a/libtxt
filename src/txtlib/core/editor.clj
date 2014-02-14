(ns txtlib.core.editor
  (:require [txtlib.core.lens :refer :all]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.frame :as frame]
            [txtlib.core.format :as format]))

(defrecord Window [buffer keymap])

(defprotocol Editor
  (frame [editor] [editor frame])
  (style [editor] [editor style])
  (render [editor renderer])
  (run [editor input]))

(defprotocol Clipboard
  (clipboard [editor] [editor clipboard]))

(defrecord Input [char key modifiers])

(def window (compose frame/current frame))

(def history (compose (lens :buffer) window))

(def buffer (compose history/present history))

(def keymap (compose (lens :keymap) window))

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
  (-> editor buffer buffer/show))

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

(defn copy [editor]
  (if-let [string (-> editor buffer buffer/copy)]
    (update editor clipboard history/commit string)
    editor))

(defn cut [editor]
  (-> editor
      copy
      (update history history/edit buffer/cut)))

(defn paste [editor]
  (insert editor :left (-> editor clipboard history/present)))

(defn undo [editor]
  (update editor history history/undo))

(defn commit [editor]
  (update editor history history/commit (buffer editor)))

(defn changed? [editor]
  (-> editor buffer buffer/changed?))

(defn open [editor path string]
  (update editor frame frame/open path (history/history (buffer/buffer string))))

(defn save [editor]
  (update editor buffer buffer/save))

(defn path [editor]
  (-> editor frame :key))

(defn compute [editor]
  (update editor style format/compute (buffer editor)))

(defn search [editor]
  (let [keymap {#{:enter} #(-> %
                               (update frame frame/switch (path editor))
                               (update buffer buffer/search :right (show %)))
                #{:backspace} #(delete % :left buffer/char)}
        minibuffer (Window. (history/history buffer/empty) keymap)]
    (update editor frame frame/open "*minibuffer*" minibuffer)))
