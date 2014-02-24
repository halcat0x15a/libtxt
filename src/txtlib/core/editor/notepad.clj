(ns txtlib.core.editor.notepad
  (:require [clojure.walk :as walk]
            [txtlib.core :refer [map-values map-keys]]
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

(defn run [editor {:keys [char code modifiers] :as input}]
  (let [keys (conj modifiers code)]
    (or (if-let [move (move (disj keys :shift))]
          (-> editor
              ((if (modifiers :shift)
                 editor/activate
                 editor/deactivate))
              move))
        (if-let [f (keymap keys)]
          (-> editor
              f
              ((if (editor/changed? editor)
                 editor/commit
                 identity))))
        (if-let [edit (edit keys)]
          (edit editor))
        (editor/input editor input))))

(def notepad (editor/editor "*scratch*" keymap style))
