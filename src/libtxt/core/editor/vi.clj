(ns libtxt.core.editor.vi
  (:require [libtxt.core :refer [map-values]]
            [libtxt.core.buffer :as buffer]
            [libtxt.core.format :as format]
            [libtxt.core.editor :as editor]
            [libtxt.core.editor.command :as command]))

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
   \h #(editor/move % buffer/right buffer/character)
   \l #(editor/move % buffer/left buffer/character)
   \w #(editor/delete % buffer/right buffer/word)
   \b #(editor/delete % buffer/left buffer/word)
   :default (fn [editor _] editor)})

(def normal->insert
  {\i identity
   \a #(editor/move % buffer/right buffer/character)
   \I #(editor/move % buffer/left buffer/characters)
   \A #(editor/move % buffer/right buffer/characters)
   \o #(-> %
           (editor/move buffer/right buffer/characters)
           (editor/insert buffer/left \newline))
   \O #(-> %
           (editor/move buffer/left buffer/characters)
           (editor/insert buffer/right \newline))})

(def normal
  {\h #(editor/move % buffer/left buffer/character)
   \j #(editor/move % buffer/right buffer/line)
   \k #(editor/move % buffer/left buffer/line)
   \l #(editor/move % buffer/right buffer/character)
   \w #(editor/move % buffer/right buffer/word)
   \b #(editor/move % buffer/left buffer/word)
   \x #(editor/delete % buffer/right buffer/character)
   \X #(editor/delete % buffer/left buffer/character)
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
