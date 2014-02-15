(ns txtlib.jvm
  (:require [txtlib.core.lens :as lens]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.format :as format]
            [txtlib.core.editor :as editor]
            [txtlib.core.keymap :as keymap]
            [txtlib.core.application :as app]
            [txtlib.core.application.notepad :as notepad])
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
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/ESCAPE :esc})

(defn input [^KeyEvent event]
  (let [code (.getCode event)]
    (keymap/input
     (first (.getText event))
     (get special code (keyword (.getName code)))
     (.isShiftDown event)
     (.isControlDown event)
     (.isAltDown event)
     (.isMetaDown event))))

(defn open [app stage]
  (if-let [file (.showOpenDialog (FileChooser.) stage)]
    (app/open app (.getPath file) (slurp file))
    app))

(defn write [app file]
  (spit file (-> app app/current editor/text))
  (-> app lens/update app/current editor/save))

(defn save-as [app stage]
  (if-let [file (.showSaveDialog (FileChooser.) stage)]
    (write app file)
    app))

(defn save [app stage]
  (if (-> app app/current editor/changed?)
    (write app (app/path app))
    app))

(defn keymap [stage]
  {#{:O :ctrl} #(open % stage)
   #{:S :ctrl} #(save % stage)})

(defn -start [this ^Stage stage]
  (let [editor (atom (update-in notepad/notepad [:keymap] merge app/keymap (keymap stage)))
        view (doto (WebView.)
               (.setContextMenuEnabled false))
        key-press (reify EventHandler
                    (handle [this event]
                      (swap! editor keymap/run (input event))
                      (-> view .getEngine (.loadContent (app/render @editor format/html)))))
        scene (doto (Scene. view)
                (.setOnKeyPressed key-press))]
    (.. view
        heightProperty
        (addListener (reify ChangeListener
                       (changed [this observable old new]
                         (swap! editor (lens/compose (lens/lens :height) editor/bounds app/current) (int (/ new 16)))))))
    (doto stage
      (.setTitle "txtlib")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch txtlib.jvm args))
