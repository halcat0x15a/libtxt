(ns txtlib.core.editor
  (:refer-clojure :exclude [read newline])
  (:require [txtlib.core.lens :refer [update lens compose]]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.map :as map]
            [txtlib.core.format :as format]))

(defrecord Event [char key modifiers])

(defn event
  ([char key modifiers]
     (Event. char key modifiers))
  ([char key shift? ctrl? alt? meta?]
     (->> {:shift shift? :ctrl ctrl? :alt alt? :meta meta?}
          (filter second)
          (map first)
          set
          (Event. char key))))

(defprotocol Buffer
  (keymap [buffer]))

(defprotocol Editor
  (read [editor string])
  (render [editor format]))

(def buffers (lens :buffers))

(def current (compose (lens :val) buffers))

(def buffer (compose (lens :buffer) current))

(def history (compose (lens :history) current))

(def bounds (compose (lens :bounds) current))

(def hint (compose (lens :hint) current))

(def clipboard (lens :clipboard))

(defn path [editor]
  (-> editor buffers :key))

(defn text [editor]
  (-> editor buffer buffer/text))

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

(defn copy [editor]
  (if-let [string (-> editor buffer buffer/copy)]
    (update editor clipboard history/commit string)
    editor))

(defn cut [editor]
  (-> editor
      copy
      (update buffer buffer/cut)))

(defn paste [editor]
  (update editor buffer buffer/insert :left (-> editor clipboard history/present)))

(defn select-all [editor]
  (-> editor
      (move :left buffer/all)
      mark
      (move :right buffer/all)))

(defn changed? [editor]
  (not= (buffer editor) (history/present (history editor))))

(defn undo [editor]
  (-> editor
      (buffer (-> editor history history/present))
      (update history history/undo)))

(defn commit [editor]
  (if (changed? editor)
    (update editor history history/commit (buffer editor))
    editor))

(defn add [{:keys [height] :as editor} key {:keys [bounds] :as window}]
  (update editor buffers map/add key window))

(defn close [editor]
  (update editor buffers map/delete (path editor)))

(defn open [editor key string]
  (-> editor
      (hint :hidden)
      (add key (read editor string))))

(defn switch
  ([editor key]
     (update editor buffers map/switch key))
  ([editor]
     (switch editor (map/next-key (buffers editor)))))

(defn compute [editor]
  (update editor bounds format/compute (buffer editor)))

(defn width [{:keys [buffers width] :as editor}]
  width)

(defn height [{:keys [buffers height] :as editor}]
  (/ (- height (->> buffers map/values (filter #(= (:hint %) :absolute)) (map (comp :height :bounds)) (apply +)))
     (->> buffers map/values (filter #(= (:hint %) :horizontal)) count)))

(defn resize [editor]
  (update editor buffers map/map-values
          (fn [buffer]
            (case (:hint buffer)
              :horizontal (assoc-in buffer [:bounds :height] (height editor))
              :hidden (assoc-in buffer [:bounds :height] 0)
              buffer))))

(defn run [editor {:keys [char key modifiers] :as event}]
  (let [{:keys [run] :as keymap} (merge (:keymap editor) (-> editor current keymap))]
    (if-let [run (or (get keymap (conj modifiers key)) (get keymap char))]
      (-> editor run compute resize)
      (-> editor (run event) compute resize))))

(defn input [editor {:keys [char]}]
  (if char
    (insert editor :left char)
    editor))
