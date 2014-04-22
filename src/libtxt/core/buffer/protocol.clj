(ns libtxt.core.buffer.protocol)

(defprotocol Buffer
  (insert-left [buffer value])
  (insert-right [buffer value])
  (delete [buffer n])
  (move [buffer n]))

(defprotocol Zipper
  (left [buffer])
  (right [buffer]))
