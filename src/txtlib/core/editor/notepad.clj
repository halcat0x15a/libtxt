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
  {#{:enter} editor/newline
   #{:backspace} editor/backspace
   #{:Z :ctrl} editor/undo
   :default editor/input})

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

(def notepad (editor/editor "*scratch*" keymap style))
