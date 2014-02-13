(ns txtlib.core.editor.notepad
  (:require [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.editor :as editor]
            [txtlib.core.format :as format]
            [txtlib.core.parser.plain :as plain]))

(def style
  (format/map->Style
   {:color (format/->Color "black" "white")
    :cursor (format/->Color "white" "black")
    :selection (format/->Color "white" "gray")
    :fontsize 16
    :x 0
    :y 0
    :width 0
    :height 0}))

(def keymap
  {#{:enter} #(-> % editor/commit (editor/insert :left \newline))
   #{:backspace} #(-> % editor/commit (editor/delete :left buffer/char))
   #{:left} #(editor/move % :left buffer/char)
   #{:right} #(editor/move % :right buffer/char)
   #{:up} #(editor/move % :left buffer/line)
   #{:down} #(editor/move % :right buffer/line)
   #{:left :shift} #(-> % editor/activate (editor/move :left buffer/char))
   #{:right :shift} #(-> % editor/activate (editor/move :right buffer/char))
   #{:up :shift} #(-> % editor/activate (editor/move :left buffer/line))
   #{:down :shift} #(-> % editor/activate (editor/move :right buffer/line))
   #{:A :ctrl} #(-> % (editor/move :left buffer/all) editor/mark (editor/move :right buffer/all))
   #{:Z :ctrl} #(-> % editor/deactivate editor/undo)
   #{:C :ctrl} #(-> % editor/copy editor/deactivate)
   #{:X :ctrl} #(-> % editor/commit editor/cut editor/deactivate)
   #{:V :ctrl} #(-> % editor/commit editor/deactivate editor/paste)})

(defrecord Notepad [buffer clipboard keymap style]
  editor/Clipboard
  (clipboard [editor] clipboard)
  (clipboard [editor clipboard] (assoc editor :clipboard clipboard))
  editor/Editor
  (buffer [editor] buffer)
  (buffer [editor buffer] (assoc editor :buffer buffer))
  (style [editor] style)
  (style [editor style] (assoc editor :style style))
  (render [editor renderer]
    (-> buffer history/present plain/parse (renderer style)))
  (run [editor {:keys [char key modifiers] :as input}]
    (if-let [f (get keymap (conj modifiers key))]
      (-> editor f editor/compute)
      (if char
        (-> editor editor/commit (editor/insert :left char) editor/compute)
        editor))))

(defn notepad [map]
  (Notepad.
   (history/history buffer/empty)
   (history/history "")
   (merge keymap map)
   style))
