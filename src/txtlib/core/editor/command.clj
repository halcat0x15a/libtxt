(ns txtlib.core.editor.command
  (:require [clojure.string :as string]
            [txtlib.core.lens :refer [lens update compose]]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.editor :as editor]))

(def function (compose (lens :function) editor/current))

(def label (compose (lens :label) editor/current))

(def target (compose (lens :target) editor/current))

(defn execute [editor]
  (apply (function editor)
         (editor/switch editor (target editor))
         (-> editor editor/buffer buffer/text (string/split #"\s+" -1))))

(def keymap
  {#{:enter} execute
   #{:backspace} editor/backspace
   #{:left} #(editor/move % :left buffer/character)
   #{:right} #(editor/move % :right buffer/character)
   :default editor/input})

(defrecord Control [buffer history keymap label function target])

(defn control [editor label function]
  (Control. buffer/empty (history/root buffer/empty) keymap label function (editor/id editor)))

(defn search [editor]
  (editor/minibuffer editor "*search*" (control editor "search" editor/search)))

(defn command [editor]
  (editor/minibuffer editor "*command*" (control editor "command" editor/execute)))
