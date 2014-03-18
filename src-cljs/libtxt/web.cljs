(ns libtxt.web
  (:require [clojure.string :as string]
            [goog.dom :as dom]
            [goog.object :as object]
            [goog.events :as events]
            [libtxt.core.format :as format]
            [libtxt.core.buffer :as buffer]
            [libtxt.core.editor :as editor]
            [libtxt.core.editor.slide :as slide]
            [libtxt.core.editor.vi :as vi])
  (:import [goog.dom ViewportSizeMonitor]
           [goog.events EventType KeyCodes KeyHandler]
           [goog.fs FileReader]))

(def editor (atom vi/vi))

(def special
  {KeyCodes.ENTER :enter
   KeyCodes.BACKSPACE :backspace
   KeyCodes.TAB :tab
   KeyCodes.LEFT :left
   KeyCodes.RIGHT :right
   KeyCodes.UP :up
   KeyCodes.DOWN :down
   KeyCodes.ESC :escape})

(defn render []
  (dom/replaceNode (dom/createDom "div" #js{"id" "libtxt"}
                     (dom/htmlToDocumentFragment (editor/render @editor format/html)))
                   (dom/getElement "libtxt")))

(defn read []
  (let [input (dom/createDom "input" #js{"type" "file"})]
    (doto input
      (events/listen EventType.CHANGE
        (fn [event]
          (let [reader (js/FileReader.)]
            (set! (.-onloadend reader)
                  (fn [event]
                    (.log js/console event)
                    (swap! editor editor/buffer (buffer/buffer (.-result reader)))
                    (render)))
            (.readAsText reader (aget (.-files input) 0)))))
      .click)))

(def system
  (reify editor/OS
    (open-dialog [system]
      (read)
      nil)
    (save-dialog [system])
    (exists [system path])
    (read [system path])
    (write [system path content])
    (exit [system])))

(defn char-code [code]
  (if (pos? code)
    (.fromCharCode js/String code)))

(defn key-code [code]
  (some-> (object/findKey KeyCodes (partial identical? code))
          string/upper-case
          keyword))

(defn main []
  (let [vsm (ViewportSizeMonitor.)
        resize (fn [event]
                 (let [size (.getSize vsm)]
                   (swap! editor assoc :height (int (/ (.-height size) 16)))
                   (render)))
        key (fn [event]
              (let [char (char-code (.-charCode event))
                    key (get special (.-keyCode event) (key-code (.-keyCode event)))
                    input (editor/event char key
                                        (.-shiftKey event)
                                        (.-ctrlKey event)
                                        (.-altKey event)
                                        (.-metaKey event))]
                (.preventDefault event)
                (binding [editor/*system* system]
                  (swap! editor editor/run input))
                (render)))]
    (swap! editor/commands assoc "slide" slide/start)
    (resize nil)
    (.addEventListener vsm EventType.RESIZE resize)
    (doto (KeyHandler. (dom/getDocument) true)
      (.addEventListener KeyHandler.EventType.KEY key))))
