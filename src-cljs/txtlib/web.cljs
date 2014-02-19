(ns txtlib.web
  (:require [clojure.string :as string]
            [goog.dom :as dom]
            [goog.object :as object]
            [goog.events :as events]
            [txtlib.core.format :as format]
            [txtlib.core.editor :as editor]
            [txtlib.core.editor.notepad :as notepad])
  (:import [goog.dom ViewportSizeMonitor]
           [goog.events EventType KeyCodes KeyHandler]))

(def special
  {KeyCodes.ENTER :enter
   KeyCodes.BACKSPACE :backspace
   KeyCodes.TAB :tab
   KeyCodes.LEFT :left
   KeyCodes.RIGHT :right
   KeyCodes.UP :up
   KeyCodes.DOWN :down
   KeyCodes.ESC :esc})

(defn main [element]
  (let [editor (atom notepad/notepad)
        element (atom element)
        vsm (ViewportSizeMonitor.)
        resize (fn [event]
                 (let [size (.getSize vsm)]
                   (swap! editor assoc :height (int (/ (.-height size) 16)))))]
    (.addEventListener vsm EventType.RESIZE resize)
    (resize nil)
    (doto (KeyHandler. (dom/getDocument) true)
      (.addEventListener KeyHandler.EventType.KEY
                         (fn [event]
                           (.preventDefault event)
                           (swap! editor
                                  editor/run
                                  (editor/event (if (pos? (.-charCode event))
                                                  (.fromCharCode js/String (.-charCode event)))
                                                (get special
                                                     (.-keyCode event)
                                                     (keyword (string/upper-case (object/findKey KeyCodes (partial identical? (.-keyCode event))))))
                                                (.-shiftKey event)
                                                (.-ctrlKey event)
                                                (.-altKey event)
                                                (.-metaKey event)))
                           (let [old @element]
                             (reset! element (dom/htmlToDocumentFragment (format/pre (editor/render @editor format/span) (:style @editor))))
                             (dom/replaceNode @element old)))))))
