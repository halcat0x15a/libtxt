(ns txtlib.core.editor.vi
  (:require [txtlib.core :refer [map-values]]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.format :as format]
            [txtlib.core.editor :as editor]
            [txtlib.core.editor.command :as command]))

(declare keymap)

(defn escape [editor]
  (editor/keymap editor keymap))

(def insert
  {#{:escape} escape
   #{:enter} editor/newline
   #{:backspace} editor/backspace
   :default editor/input})

(def delete
  {#{:escape} escape
   \h #(editor/move % :right buffer/character)
   \l #(editor/move % :left buffer/character)
   \w #(editor/delete % :right buffer/word)
   \b #(editor/delete % :left buffer/word)
   :default (fn [editor _] editor)})

(def normal->insert
  {\i identity
   \a #(editor/move % :right buffer/character)
   \I #(editor/move % :left buffer/characters)
   \A #(editor/move % :right buffer/characters)
   \o #(-> % (editor/move :right buffer/characters) (editor/insert :left \newline))
   \O #(-> % (editor/move :left buffer/characters) (editor/insert :right \newline))})

(def normal
  {\h #(editor/move % :left buffer/character)
   \j #(editor/move % :right buffer/line)
   \k #(editor/move % :left buffer/line)
   \l #(editor/move % :right buffer/character)
   \w #(editor/move % :right buffer/word)
   \b #(editor/move % :left buffer/word)
   \x #(editor/delete % :right buffer/character)
   \X #(editor/delete % :left buffer/character)
   \u editor/undo
   \y editor/copy
   \p editor/paste
   \v editor/activate
   \d #(editor/keymap % (map-values (partial comp escape) delete))
   \/ command/search
   \: command/command
   :default (fn [editor _] editor)})

(def keymap (merge normal (map-values (partial comp #(editor/keymap % insert)) normal->insert)))

(def vi (editor/editor "*scratch*" keymap format/style))
