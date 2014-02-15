(ns txtlib.core.application
  (:require [txtlib.core.lens :refer :all]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.frame :as frame]
            [txtlib.core.editor :as editor]
            [txtlib.core.format :as format]
            [txtlib.core.keymap :as keymap]))

(defprotocol Application
  (editor [app string])
  (frame [app] [app frame])
  (style [app] [app style])
  (clipboard [frame] [frame clipboard])
  (render [app renderer]))

(def current (compose frame/current frame))

(def buffer (compose editor/buffer current))

(defn open [app path string]
  (update app frame frame/open path (editor app string)))

(defn path [app]
  (-> app frame :key))

(defn copy [app]
  (if-let [string (-> app buffer buffer/copy)]
    (update app clipboard history/commit string)
    editor))

(defn cut [app]
  (-> app
      copy
      (update buffer buffer/cut)))

(defn paste [app]
  (update app current editor/insert :left (-> app clipboard history/present)))

(def keymap
  {:run (fn [app input]
          (update app current keymap/run input))})
