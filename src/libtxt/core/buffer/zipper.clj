(ns libtxt.core.buffer.zipper
  (:refer-clojure :exclude [empty])
  (:require [libtxt.core.buffer.protocol :as protocol]))

(defrecord Buffer [left right mark]
  protocol/Buffer
  (insert-left [buffer value]
    (with-meta (Buffer. (str left value) right mark)
      (meta buffer)))
  (insert-right [buffer value]
    (with-meta (Buffer. left (str value right) mark)
      (meta buffer)))
  (delete [buffer n]
    (let [cursor (count left)
          left (subs left 0 (min cursor (+ cursor n)))
          right (subs right (max 0 n))]
      (with-meta (Buffer. left right mark)
        (meta buffer))))
  (move [buffer n]
    (let [text (str left right)
          cursor (+ (count left) n)]
      (with-meta (Buffer. (subs text 0 cursor) (subs text cursor) mark)
        (meta buffer))))
  protocol/Zipper
  (left [buffer] left)
  (right [buffer] right))

(defn buffer
  ([string]
     (buffer "" string))
  ([left right]
     (Buffer. left right nil)))
