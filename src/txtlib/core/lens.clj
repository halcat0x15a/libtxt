(ns txtlib.core.lens)

(defn lens [key]
  (fn
    ([obj]
       (get obj key))
    ([obj value]
       (assoc obj key value))))

(defn update [obj lens f & args]
  (lens obj (apply f (lens obj) args)))

(defn compose [f g]
  (fn
    ([obj]
       (f (g obj)))
    ([obj value]
       (g obj (f (g obj) value)))))
