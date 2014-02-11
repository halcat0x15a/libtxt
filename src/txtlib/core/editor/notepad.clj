(ns txtlib.core.editor.notepad
  (:require [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.editor :as editor]
            [txtlib.core.format :as format]
            [txtlib.core.parser.plain :as plain]))

(def style
  {:cursor (format/->Color "white" "black")
   :selection (format/->Color "white" "gray")
   :symbol (format/->Color "blue" "white")
   :string (format/->Color "maroon" "white")
   :keyword (format/->Color "aqua" "white")
   :special (format/->Color "magenta" "white")
   :default (format/->Color "black" "white")})

(def keymap
  {:left #(editor/move % :left buffer/char)
   :right #(editor/move % :right buffer/char)
   :up #(editor/move % :left buffer/line)
   :down #(editor/move % :right buffer/line)})

(defrecord Notepad [current clipboard]
  editor/Editor
  (render [editor renderer]
    (renderer style (-> current history/present plain/parser)))
  (run [editor input]
    (if-let [f (get keymap input)]
      (f editor)
      (if (char? input)
        (editor/insert editor :left input)
        editor))))

(def notepad (Notepad. (history/history buffer/empty) (history/history "")))
