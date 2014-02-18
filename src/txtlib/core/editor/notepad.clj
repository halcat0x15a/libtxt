(ns txtlib.core.editor.notepad
  (:require [clojure.walk :as walk]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.map :as map]
            [txtlib.core.editor :as editor]
            [txtlib.core.editor.command :as command]
            [txtlib.core.format :as format]))

(def style
  {:color (format/->Color "black" "white")
   :cursor (format/->Color "white" "black")
   :selection (format/->Color "white" "gray")
   :fontsize 16})

(def edit
  {#{:enter} (comp editor/newline editor/commit)
   #{:backspace} editor/backspace
   #{:Z :ctrl} editor/undo
   :run (fn [editor {:keys [char]}]
          (if char
            (editor/insert editor :left char)
            editor))})

(def keymap
  (->> {#{:left} (comp #(editor/move % :left buffer/char) editor/deactivate)
        #{:right} (comp #(editor/move % :right buffer/char) editor/deactivate)
        #{:up} (comp #(editor/move % :left buffer/line) editor/deactivate)
        #{:down} (comp #(editor/move % :right buffer/line) editor/deactivate)
        #{:left :shift} (comp #(editor/move % :left buffer/char) editor/activate)
        #{:right :shift} (comp #(editor/move % :right buffer/char) editor/activate)
        #{:up :shift} (comp #(editor/move % :left buffer/line) editor/activate)
        #{:down :shift} (comp #(editor/move % :right buffer/line) editor/activate)
        #{:A :ctrl} editor/select-all
        #{:C :ctrl} editor/copy
        #{:X :ctrl} editor/cut
        #{:V :ctrl} editor/paste
        #{:F :ctrl} command/search}
       (walk/walk (fn [[k f]] [k (comp f editor/commit)]) identity)
       (merge edit)))

(defrecord Buffer [buffer history bounds]
  editor/Buffer
  (keymap [editor] keymap)
  (hint [editor] :horizontal))

(defrecord Notepad [buffers clipboard style keymap width height]
  editor/Editor
  (read [editor string]
    (Buffer. (buffer/buffer string) (history/history buffer/empty) (editor/bounds editor)))
  (render [editor format]
    (-> buffers
        (map/map-vals (fn [{:keys [buffer bounds]}]
                        (-> buffer format/buffer (format/render format style) (format/view bounds))))
        (map/reduce-vals #(str %1 \newline %2)))))

(def notepad
  (Notepad.
   (map/create "*scratch*" (Buffer. buffer/empty (history/history buffer/empty) (format/rectangle)))
   (history/history "")
   style
   {}
   0
   0))
