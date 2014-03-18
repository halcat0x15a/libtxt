(ns libtxt.web
  (:require [clojure.string :as string]
            [goog.dom :as dom]
            [goog.object :as object]
            [goog.events :as events]
            [libtxt.core.format :as format]
            [libtxt.core.editor :as editor]
            [libtxt.core.editor.notepad :as notepad])
  (:import [goog.dom ViewportSizeMonitor]
           [goog.events EventType KeyCodes KeyHandler]
           [goog.fs FileReader]))

(def special
  {KeyCodes.ENTER :enter
   KeyCodes.BACKSPACE :backspace
   KeyCodes.TAB :tab
   KeyCodes.LEFT :left
   KeyCodes.RIGHT :right
   KeyCodes.UP :up
   KeyCodes.DOWN :down
   KeyCodes.ESC :esc})

(defn render [editor]
  (dom/replaceNode (dom/htmlToDocumentFragment (editor/render editor format/html))
                   (dom/getElement "libtxt")))

(defn char-code [code]
  (if (pos? code)
    (.fromCharCode js/String code)))

(defn key-code [code]
  (-> (object/findKey KeyCodes (partial identical? code))
      string/upper-case
      keyword))

(def file (dom/createDom "input" #js{"type" "file"}))

(defn read [file]
  (doto (FileReader.)
    (.addEventListener FileReader.EventType.LOAD_END (fn [result] (.log js/console result)))
    (.readAsText file)))

(defn main []
  (let [editor (atom notepad/notepad)
        vsm (ViewportSizeMonitor.)
        resize (fn [event]
                 (let [size (.getSize vsm)]
                   (swap! editor assoc :height (int (/ (.-height size) 16)))
                   (render @editor)))
        key (fn [event]
              (let [char (char-code (.-charCode event))
                    key (get special (.-keyCode event) (key-code (.-keyCode event)))
                    input (editor/event char key
                                        (.-shiftKey event)
                                        (.-ctrlKey event)
                                        (.-altKey event)
                                        (.-metaKey event))]
                (.preventDefault event)
                (swap! editor editor/run input)
                (render @editor)))]
    (resize nil)
    (.addEventListener vsm EventType.RESIZE resize)
    (doto (KeyHandler. (dom/getDocument) true)
      (.addEventListener KeyHandler.EventType.KEY key))))
