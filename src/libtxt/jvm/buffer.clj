(ns libtxt.jvm.buffer
  (:require [libtxt.core.buffer.protocol :as protocol])
  (:import [java.nio CharBuffer]))

(def ^:dynamic *gap* 1024)

(defn allocate [^String value]
  (-> (CharBuffer/allocate (+ (.length value) *gap*))
      (.mark)
      ^CharBuffer (.position *gap*)
      (.put value)
      (.reset)
      (.limit *gap*)))

(defn ^CharBuffer tail [^CharBuffer buffer]
  (-> buffer
      (.position (.limit buffer))
      (.limit (.capacity buffer))))
  
(defn preserve [^CharBuffer buffer delay]
  (let [position (.position buffer)
        limit (.limit buffer)
        result @delay]
    (.position buffer position)
    (.limit buffer limit)
    result))

(defn ^String left [^CharBuffer buffer]
  (preserve buffer (delay (-> buffer .flip .toString))))

(defn ^String right [^CharBuffer buffer]
  (preserve buffer (delay (-> buffer tail .toString))))

(defn ^CharBuffer expand
  ([^CharBuffer buffer]
     (expand buffer *gap*))
  ([^CharBuffer buffer n]
     (let [limit (+ (.limit buffer) n)]
       (-> (CharBuffer/allocate (+ (.capacity buffer) n))
           (.put (left buffer))
           (.mark)
           ^CharBuffer (.position limit)
           (.put (right buffer))
           (.reset)
           (.limit limit)))))

(defn insert [^CharBuffer buffer ^String value]
  (-> buffer
      (cond-> (< (.remaining buffer) (count value)) expand)
      (.put value)))

(defn delete-right [^CharBuffer buffer n]
  (.limit buffer (+ (.limit buffer) n)))

(defn delete-left [^CharBuffer buffer n]
  (.position buffer (- (.position buffer) n)))

(defn delete [^CharBuffer buffer n]
  (cond-> buffer
    (pos? n) (delete-right n)
    (neg? n) (delete-left (- n))))

(defn move-right [^CharBuffer buffer n]
  (let [post (char-array n)
        limit (.limit buffer)]
    (-> buffer
        (.mark)
        (.position limit)
        ^CharBuffer (.limit (+ limit n))
        (.get post)
        ^CharBuffer (.reset)
        (.put post))))

(defn move-left [^CharBuffer buffer n]
  (let [pre (char-array n)
        limit' (- (.limit buffer) n)]
    (-> buffer
        (.position (- (.position buffer) n))
        ^CharBuffer (.mark)
        (.get pre)
        ^CharBuffer (.position limit')
        (.put pre)
        .reset
        (.limit limit'))))

(defn move [^CharBuffer buffer n]
  (cond-> buffer
    (pos? n) (move-right n)
    (neg? n) (move-left (- n))))

(defrecord Buffer [^CharBuffer buffer]
  protocol/Buffer
  (insert-left [this value]
    (with-meta (Buffer. (insert buffer value))
      (meta this)))
  (insert-right [this value]
    (with-meta (Buffer. (-> buffer (insert value) (move (- (count value)))))
      (meta this)))
  (delete [this n]
    (with-meta (Buffer. (delete buffer n))
      (meta this)))
  (move [this n]
    (with-meta (Buffer. (move buffer n))
      (meta this)))
  protocol/Zipper
  (left [this]
    (left buffer))
  (right [this]
    (right buffer)))

(defn buffer [^String value]
  (Buffer. (allocate value)))
