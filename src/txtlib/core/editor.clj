(ns txtlib.core.editor
  (:refer-clojure :exclude [read newline])
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [txtlib.core.lens :refer [update compose lens zipper]]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.geometry :as geometry]
            [txtlib.core.format :as format]))

(defrecord Window [id bounds])

(defn window [id]
  (Window. id (geometry/rect)))

(defrecord Control [buffer history keymap])

(defn control [string keymap]
  (Control. (buffer/buffer string) (history/root buffer/empty) keymap))

(def frame (lens :frame))

(def focus (compose zipper frame))

(def id (compose (lens :id) focus))

(def bounds (compose (lens :bounds) focus))

(def buffers (lens :buffers))

(defn current
  ([editor]
     (get (buffers editor) (id editor)))
  ([editor buffer]
     (update editor buffers assoc (id editor) buffer)))

(def buffer (compose (lens :buffer) current))

(def history (compose (lens :history) current))

(def keymap (compose (lens :keymap) current))

(def clipboard (lens :clipboard))

(def width (lens :width))

(def height (lens :height))

(defrecord Editor [frame buffers clipboard style width height])

(defn text [editor]
  (-> editor buffer buffer/text))

(defn editor [key keymap style]
  (Editor. (zip/down (zip/vector-zip [(window key)]))
           {key (control "" keymap)}
           (history/root "")
           style
           0 0))

(defn insert [editor key value]
  (update editor buffer buffer/insert key value))

(defn newline [editor]
  (insert editor :left \newline))

(defn delete [editor key regex]
  (update editor buffer buffer/matches buffer/delete key regex))

(defn backspace [editor]
  (delete editor :left buffer/character))

(defn move [editor key regex]
  (update editor buffer buffer/matches buffer/move key regex))

(defn search [editor query]
  (update editor buffer buffer/search :right query))

(defn mark [editor]
  (update editor buffer buffer/mark))

(defn activate [editor]
  (update editor buffer buffer/activate))

(defn deactivate [editor]
  (update editor buffer buffer/deactivate))

(defn select-all [editor]
  (-> editor
      (move :left buffer/all)
      mark
      (move :right buffer/all)))

(defn copy [editor]
  (if-let [string (-> editor buffer buffer/copy)]
    (update editor clipboard history/commit string)
    editor))

(defn cut [editor]
  (-> editor copy (update buffer buffer/cut)))

(defn paste [editor]
  (insert editor :left (-> editor clipboard zip/node)))

(defn undo [editor]
  (-> editor
      (buffer (-> editor history zip/node))
      (update history history/undo)))

(defn changed? [editor]
  (not= (text editor)
        (-> editor history zip/node buffer/text)))

(defn commit [editor]
  (if (changed? editor)
    (update editor history history/commit (buffer editor))
    editor))

(defn switch [editor key]
  (if (some #(= (:id %) key) (-> editor frame zip/root))
    (loop [windows (-> editor frame zip/leftmost)]
      (if (= (:id (zip/node windows)) key)
        (frame editor windows)
        (recur (zip/right frame))))
    editor))

(defn minibuffer [editor key control]
  (-> editor
      (update frame zip/insert-right (Window. key (geometry/rect 0 0 (width editor) 1)))
      (update frame zip/right)
      (update buffers assoc key control)))

(defn open [editor key string]
  (-> editor
      (update buffers assoc key (control string (keymap editor)))
      (id key)))

(defn compute [editor]
  (-> editor
      (update bounds assoc :width (width editor))
      (update bounds assoc :height (height editor))
      (update bounds geometry/fix (-> editor buffer buffer/position))))

(defprotocol Input
  (character [input])
  (code [input])
  (modifiers [input]))

(deftype Event [char code modifiers]
  Input
  (character [input] char)
  (code [input] code)
  (modifiers [input] modifiers))

(defn event
  ([char code modifiers]
     (Event. char code modifiers))
  ([char code shift? ctrl? alt? meta?]
     (->> {:shift shift? :ctrl ctrl? :alt alt? :meta meta?}
          (filter val)
          (map key)
          set
          (Event. char code))))

(defn run [editor input]
  (let [{:keys [default] :as keymap} (keymap editor)]
    (compute (if-let [f (get keymap (conj (modifiers input) (code input)) (get keymap (character input)))]
               (f editor)
               (default editor input)))))

(defn input [editor input]
  (let [char (character input)]
    (if (and char (empty? (disj (modifiers input) :shift)))
      (insert editor :left char)
      editor)))

(defn render [editor format]
  (let [render (fn [control bounds]
                 (geometry/view bounds (-> control :buffer format/buffer (format/render format (:style editor)))))
        frame (frame editor)]
    (->> frame
         zip/root
         (map #(render (get (buffers editor) (:id %)) (:bounds %)))
         (string/join \newline))))
