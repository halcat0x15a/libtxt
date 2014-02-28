(ns txtlib.core.editor.notepad
  (:require [txtlib.core :refer [map-values map-keys]]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.editor :as editor]
            [txtlib.core.editor.command :as command]
            [txtlib.core.format :as format]))

(def edit
  {#{:enter} editor/newline
   #{:backspace} editor/backspace
   :default editor/input})

(def move
  {#{:left} #(editor/move % :left buffer/character)
   #{:right} #(editor/move % :right buffer/character)
   #{:up} #(editor/move % :left buffer/line)
   #{:down} #(editor/move % :right buffer/line)
   #{:left :alt} #(editor/move % :left buffer/word)
   #{:right :alt} #(editor/move % :right buffer/word)})

(def operation
  {#{:A :ctrl} editor/select-all
   #{:C :ctrl} editor/copy
   #{:X :ctrl} editor/cut
   #{:V :ctrl} editor/paste
   #{:Z :ctrl} editor/undo
   #{:F :ctrl} command/search})

(def keymap
  (merge (map-values #(comp editor/commit %) operation)
         (map-values #(comp % editor/deactivate) move)
         (->> move (map-keys #(conj % :shift)) (map-values #(comp % editor/activate)))
         edit))

(def notepad (editor/editor "*scratch*" keymap format/style))
