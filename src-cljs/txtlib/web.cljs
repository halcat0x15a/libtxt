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

(defn main []
  (let [editor (atom notepad/notepad)
        vsm (ViewportSizeMonitor.)
        update! (fn [f & args]
                  (apply swap! editor f args)
                  (dom/replaceNode (dom/htmlToDocumentFragment (format/pre (editor/render @editor format/span) (:style @editor)))
                                   (dom/getElement "txtlib")))
        resize (fn [event]
                 (let [size (.getSize vsm)]
                   (update! editor assoc :height (int (/ (.-height size) 16)))))
        key (fn [event]
              (let [char (if (pos? (.-charCode event))
                           (.fromCharCode js/String (.-charCode event)))
                    key (get special
                             (.-keyCode event)
                             (keyword (string/upper-case (object/findKey KeyCodes (partial identical? (.-keyCode event))))))]
                (.preventDefault event)
                (update! editor editor/run (editor/event char key (.-shiftKey event) (.-ctrlKey event) (.-altKey event) (.-metaKey event)))))]
    (resize nil)
    (.addEventListener vsm EventType.RESIZE resize)
    (doto (KeyHandler. (dom/getDocument) true)
      (.addEventListener KeyHandler.EventType.KEY key))))
