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
   #{:W :ctrl} editor/close
   #{:tab :ctrl} editor/switch
   :run editor/input})

(def keymap
  (->> {#{:left} (comp #(editor/move % :left buffer/character) editor/deactivate)
        #{:right} (comp #(editor/move % :right buffer/character) editor/deactivate)
        #{:up} (comp #(editor/move % :left buffer/line) editor/deactivate)
        #{:down} (comp #(editor/move % :right buffer/line) editor/deactivate)
        #{:left :shift} (comp #(editor/move % :left buffer/character) editor/activate)
        #{:right :shift} (comp #(editor/move % :right buffer/character) editor/activate)
        #{:up :shift} (comp #(editor/move % :left buffer/line) editor/activate)
        #{:down :shift} (comp #(editor/move % :right buffer/line) editor/activate)
        #{:A :ctrl} editor/select-all
        #{:C :ctrl} editor/copy
        #{:X :ctrl} editor/cut
        #{:V :ctrl} editor/paste
        #{:F :ctrl} command/search}
       (walk/walk (fn [[k f]] [k (comp f editor/commit)]) identity)
       (merge edit)))

(defrecord Buffer [buffer history bounds hint]
  editor/Buffer
  (keymap [editor] keymap))

(defrecord Notepad [buffers clipboard style keymap width height]
  editor/Editor
  (read [editor string]
    (Buffer. (buffer/buffer string) (history/history buffer/empty) (editor/bounds editor) :horizontal))
  (render [editor format]
    (-> buffers
        (map/map-values (fn [{:keys [buffer bounds]}]
                          (-> buffer format/buffer (format/render format style) (format/view bounds))))
        (map/reduce-values #(str %1 \newline %2)))))

(def notepad
  (Notepad.
   (map/create "*scratch*" (Buffer. buffer/empty (history/history buffer/empty) (format/rectangle) :horizontal))
   (history/history "")
   style
   {}
   0
   0))
