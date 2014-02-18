(ns txtlib.jvm
  (:require [txtlib.core.lens :as lens]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.format :as format]
            [txtlib.core.editor :as editor]
            [txtlib.core.editor.notepad :as notepad])
  (:gen-class :extends javafx.application.Application)
  (:import [javafx.application Application]
           [javafx.beans.value ChangeListener]
           [javafx.event EventHandler]
           [javafx.stage Stage FileChooser]
           [javafx.scene Scene]
           [javafx.scene.web WebView WebEngine]
           [javafx.scene.input KeyCode KeyEvent]))

(def special
  {KeyCode/BACK_SPACE :backspace
   KeyCode/ENTER :enter
   KeyCode/TAB :tab
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/ESCAPE :esc})

(defn input [^KeyEvent event]
  (let [code (.getCode event)]
    (editor/event
     (first (.getText event))
     (get special code (keyword (.getName code)))
     (.isShiftDown event)
     (.isControlDown event)
     (.isAltDown event)
     (.isMetaDown event))))

(defn open [editor stage]
  (if-let [file (.showOpenDialog (FileChooser.) stage)]
    (editor/open editor (.getPath file) (slurp file))
    editor))

(defn write [editor file]
  (spit file (editor/text editor))
  editor)

(defn save-as [editor stage]
  (if-let [file (.showSaveDialog (FileChooser.) stage)]
    (write editor file)
    editor))

(defn save [editor stage]
  (if (editor/changed? editor)
    (write editor (editor/path editor))
    editor))

(defn keymap [stage]
  {#{:O :ctrl} #(open % stage)
   #{:S :ctrl} #(save % stage)})

(defn -start [this ^Stage stage]
  (let [editor (atom (assoc notepad/notepad :keymap (keymap stage)))
        view (doto (WebView.)
               (.setContextMenuEnabled false))
        key-press (reify EventHandler
                    (handle [this event]
                      (swap! editor editor/run (input event))
                      (-> view .getEngine (.loadContent (format/pre (editor/render @editor format/span) (:style @editor))))))
        scene (doto (Scene. view)
                (.setOnKeyPressed key-press))]
    (.. view
        heightProperty
        (addListener (reify ChangeListener
                       (changed [this observable old new]
                         (swap! editor assoc :height (int (/ new 16)))
                         (swap! editor editor/resize)))))
    (doto stage
      (.setTitle "txtlib")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch txtlib.jvm args))
