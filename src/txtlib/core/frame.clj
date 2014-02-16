(ns txtlib.core.frame)

(defrecord Frame [key map])

(defn frame [key value & others]
  (Frame. key (assoc (apply hash-map others) key value)))

(defn current
  ([{:keys [key map]}]
     (map key))
  ([{:keys [key map] :as frame} value]
     (assoc-in frame [:map key] value)))

(defn add [frame name value]
  (-> frame
      (assoc-in [:map name] value)
      (assoc :key name)))

(defn switch [{:keys [map] :as frame} name]
  (if (contains? map name)
    (assoc frame :key name)
    frame))
