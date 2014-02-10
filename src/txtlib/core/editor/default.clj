(ns txtlib.core.editor.default
  (:require [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.editor :as editor]))

(def keymap
  {:left #(editor/move % :left buffer/char)
   :right #(editor/move % :right buffer/char)
   :up #(editor/move % :left buffer/line)
   :down #(editor/move % :right buffer/line)})

(defrecord Editor [current clipboard]
  editor/Editor
  (run [editor input]
    (if-let [f (get keymap input)]
      (f editor)
      (if (char? input)
        (editor/insert editor :left input)
        editor))))

(def editor (Editor. (history/history buffer/empty) (history/history "")))
