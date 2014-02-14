(ns txtlib.core.window)

(defprotocol Window
  (buffer [window] [window buffer])
  (run [window input]))

(defprotocol History
  (history [editor] [editor history]))

(defrecord Input [char key modifiers])

(defn input
  ([char key modifiers]
     (Input. char key modifiers))
  ([char key shift? ctrl? alt?]
     (->> {:shift shift? :ctrl ctrl? :alt alt?}
          (filter second)
          (map first)
          set
          (Input. char key))))
