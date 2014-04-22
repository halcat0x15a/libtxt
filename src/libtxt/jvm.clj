(ns libtxt.jvm
  (:require [clojure.java.io :as io]
            [libtxt.core.lens :as lens]
            [libtxt.core.buffer :as buffer]
            [libtxt.core.history :as history]
            [libtxt.core.format :as format]
            [libtxt.core.editor :as editor]
            [libtxt.core.editor.slide :as slide]
            [libtxt.core.editor.notepad :as notepad]
            [libtxt.core.editor.vi :as vi])
  (:gen-class :extends javafx.application.Application)
  (:import [javafx.application Application Platform]
           [javafx.beans.value ChangeListener]
           [javafx.event EventHandler]
           [javafx.stage Stage FileChooser]
           [javafx.scene Scene]
           [javafx.scene.web WebView WebEngine]
           [javafx.scene.input KeyCode KeyEvent]))

(def ^:dynamic *stage*)

(defn system []
  (reify editor/OS
    (open-dialog [system]
      (.showOpenDialog (FileChooser.) *stage*))
    (save-dialog [system]
      (.showSaveDialog (FileChooser.) *stage*))
    (exists [system path]
      (.exists (io/file path)))
    (read [system path]
      (slurp path))
    (write [system path content]
      (spit path content))
    (exit [system]
      (Platform/exit))))

(def special
  {KeyCode/BACK_SPACE :backspace
   KeyCode/ENTER :enter
   KeyCode/TAB :tab
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/ESCAPE :escape})

(defn input [^KeyEvent event]
  (let [code (.getCode event)]
    (editor/event
     (first (.getText event))
     (get special code (keyword (.getName code)))
     (.isShiftDown event)
     (.isControlDown event)
     (.isAltDown event)
     (.isMetaDown event))))

(defn -start [this ^Stage stage]
  (let [editor (atom vi/vi)
        view (doto (WebView.)
               (.setContextMenuEnabled false))
        key-press (reify EventHandler
                    (handle [this event]
                      (binding [*stage* stage
                                editor/*system* (system)]
                        (swap! editor editor/run (input event)))
                      (-> view .getEngine (.loadContent (editor/render @editor format/html)))))
        scene (doto (Scene. view)
                (.setOnKeyPressed key-press))]
    (swap! editor/commands assoc "slide" slide/start)
    (.. view
        heightProperty
        (addListener (reify ChangeListener
                       (changed [this observable old new]
                         (swap! editor assoc :height (int (/ new 16)))
                         (swap! editor editor/compute)))))
    (doto stage
      (.setTitle "libtxt")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch libtxt.jvm args))
