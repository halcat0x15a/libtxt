(ns txtlib.core.editor.command
  (:require [txtlib.core.lens :refer :all]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.format :as format]
            [txtlib.core.editor :as editor]))

(def function (compose (lens :function) editor/current))

(def parameters (compose (lens :parameters) editor/current))

(def target (compose (lens :target) editor/current))

(defn execute [editor]
  (let [[_ & parameters'] (parameters editor)
        argument (-> editor editor/buffer buffer/text)]
    (if (empty? parameters')
      ((function editor) (editor/switch editor (target editor)) argument)
      (-> editor
          (update function #(fn [editor argument] (partial % editor argument)))
          (parameters parameters')))))

(def keymap
  {#{:enter} execute
   :run (fn [editor {:keys [char]}]
          (if char
            (editor/insert editor char)
            editor))})

(defrecord Command [buffer history bounds target function parameters]
  editor/Buffer
  (keymap [command] keymap))

(defn command [editor function & parameters]
  (Command. buffer/empty (history/history buffer/empty) (editor/bounds editor) (editor/path editor) function parameters))

(defn search [editor]
  (editor/add editor "*search*" (command editor editor/search "query")))
