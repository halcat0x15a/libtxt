(ns txtlib.jvm
  (:require [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.format :as format]
            [txtlib.core.editor :as editor]
            [txtlib.core.editor.notepad :as notepad])
  (:gen-class :extends javafx.application.Application)
  (:import [javafx.application Application]
           [javafx.event EventHandler]
           [javafx.stage Stage FileChooser]
           [javafx.scene Scene]
           [javafx.scene.web WebView WebEngine]
           [javafx.scene.input KeyCode KeyEvent]))

(def special
  {KeyCode/BACK_SPACE :bs
   KeyCode/ENTER :enter
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/ESCAPE :esc})

(defn input [^KeyEvent event]
  (let [code (.getCode event)]
    (editor/input
     (first (.getText event))
     (get special code (keyword (.getName code)))
     (.isShiftDown event)
     (.isControlDown event)
     (.isAltDown event))))

(defn open [editor stage]
  (if-let [file (.showOpenDialog (FileChooser.) stage)]
    (editor/open editor (.getPath file) (slurp file))
    editor))

(defn write [editor file]
  (spit file (editor/show editor))
  (editor/save editor))

(defn save [editor stage]
  (if (editor/changed? editor)
    (if-let [file (editor/path editor)]
      (write editor file)
      (if-let [file (.showSaveDialog (FileChooser.) stage)]
        (write editor file)
        editor))
    editor))

(defn keymap [stage]
  {#{:O :ctrl} #(open % stage)
   #{:S :ctrl} #(save % stage)})

(defn -start [this ^Stage stage]
  (let [editor (atom (notepad/notepad (keymap stage)))
        view (doto (WebView.)
               (.setContextMenuEnabled false))
        handler (reify EventHandler
                  (handle [this event]
                    (swap! editor editor/run (input event))
                    (-> view .getEngine (.loadContent (editor/render @editor format/html)))))
        scene (doto (Scene. view)
                (.setOnKeyPressed handler))]
    (doto stage
      (.setTitle "txtlib")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch txtlib.jvm args))
