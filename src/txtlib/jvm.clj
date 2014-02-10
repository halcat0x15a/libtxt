(ns txtlib.jvm
  (:require [txtlib.core.editor :as editor]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.editor.default :refer [editor]])
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application]
           [javafx.event EventHandler]
           [javafx.stage Stage]
           [javafx.scene Scene]
           [javafx.scene.web WebView WebEngine]
           [javafx.scene.input KeyCode KeyEvent]))

(defn input [^KeyEvent event]
  {KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/ESCAPE :esc})

(defn -start [this ^Stage stage]
  (let [editor (atom editor)
        view (doto (WebView.)
               (.setContextMenuEnabled false))
        handler (reify EventHandler
                  (handle [this event]
                    (swap! editor editor/run (first (.getText ^KeyEvent event)))
                    (-> view .getEngine (.loadContent (editor/show @editor)))))
        scene (doto (Scene. view)
                (.setOnKeyPressed handler))]
    (doto stage
      (.setTitle "txtlib")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch txtlib.jvm args))
