(ns txtlib.core.application.notepad
  (:require [clojure.walk :as walk]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.frame :as frame]
            [txtlib.core.editor :as editor]
            [txtlib.core.format :as format]
            [txtlib.core.parser.plain :as plain]
            [txtlib.core.keymap :as keymap]
            [txtlib.core.application :as app]))

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
  {#{:enter} #(editor/insert % :left \newline)
   #{:backspace} #(editor/delete % :left buffer/char)
   #{:left} #(-> % editor/deactivate (editor/move :left buffer/char))
   #{:right} #(-> % editor/deactivate (editor/move :right buffer/char))
   #{:up} #(-> % editor/deactivate (editor/move :left buffer/line))
   #{:down} #(-> % editor/deactivate (editor/move :right buffer/line))
   #{:left :shift} #(-> % editor/activate (editor/move :left buffer/char))
   #{:right :shift} #(-> % editor/activate (editor/move :right buffer/char))
   #{:up :shift} #(-> % editor/activate (editor/move :left buffer/line))
   #{:down :shift} #(-> % editor/activate (editor/move :right buffer/line))
   #{:A :ctrl} editor/select-all
   #{:Z :ctrl} editor/undo
   :run (fn [editor {:keys [char]}]
          (if char
            (editor/insert editor :left char)
            editor))})

(def global
  {#{:C :ctrl} app/copy
   #{:X :ctrl} app/cut
   #{:V :ctrl} app/paste})

(deftype Editor [buffer history bounds]
  editor/Editor
  (buffer [editor] buffer)
  (buffer [editor buffer] (Editor. buffer history bounds))
  (history [editor] history)
  (history [editor history] (Editor. buffer history bounds))
  (bounds [editor] bounds)
  (bounds [editor bounds] (Editor. buffer history bounds))
  keymap/Application
  (keymap [editor]
    (walk/walk (fn [[k f]] [k (comp editor/compute editor/commit f)]) identity keymap)))

(defrecord Notepad [frame clipboard style keymap]
  app/Application
  (editor [app string]
    (Editor. (buffer/buffer string) (history/history buffer/empty) (-> app app/current editor/bounds)))
  (frame [app] frame)
  (frame [app frame] (assoc app :frame frame))
  (clipboard [app] clipboard)
  (clipboard [app clipboard] (assoc app :clipboard clipboard))
  (style [app] style)
  (style [app style] (assoc app :style style))
  (render [app renderer]
    (let [editor (app/current app)]
      (-> editor editor/buffer plain/parse (renderer style (editor/bounds editor)))))
  keymap/Application
  (keymap [app] (merge global keymap)))

(def notepad
  (Notepad.
   (frame/frame "*scratch*" (Editor. buffer/empty (history/history buffer/empty) (format/->Rectangle 0 0 0 0)))
   (history/history "")
   style
   app/keymap))
