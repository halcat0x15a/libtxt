(ns txtlib.core.keymap)

(defprotocol Application
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

(defn run [application {:keys [char key modifiers] :as input}]
  (let [{:keys [run] :as keymap} (keymap application)]
    (if-let [run (or (get keymap (conj modifiers key)) (get keymap char))]
      (run application)
      (run application input))))
