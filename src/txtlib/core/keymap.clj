(ns txtlib.core.keymap)

(defprotocol Window
  (keymap [application]))

(defrecord Input [char key modifiers])

(defn input
  ([char key modifiers]
     (Input. char key modifiers))
  ([char key shift? ctrl? alt? meta?]
     (->> {:shift shift? :ctrl ctrl? :alt alt? :meta meta?}
          (filter second)
          (map first)
          set
          (Input. char key))))

(defn run [window {:keys [char key modifiers] :as input}]
  (let [{:keys [run] :as keymap} (keymap window)]
    (if-let [run (or (get keymap (conj modifiers key)) (get keymap char))]
      (run window)
      (run window input))))
