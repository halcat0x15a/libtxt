(ns libtxt.core.markdown
  (:refer-clojure :exclude [compile])
  (:require [clojure.string :as string]
            [libtxt.core.parser :as parser]
            [libtxt.core.html :as html]))

(defn indent [size]
  (parser/regex (re-pattern (str "^\\t{" size "}"))))

(def space (parser/regex #"\s+"))

(def end (parser/map first (parser/regex #"^(\n|\Z)")))

(def anchor
  (parser/map
    (fn [[_ text ref]]
      (html/->Element "p" (html/->Element "a" text {"href" ref}) {}))
    (parser/regex #"^\[(\S+)\]\((\S+)\)")))

(def emphasis
  (parser/map
    (fn [[_ text]]
      (html/->Element "em" text {}))
    (parser/regex #"^\*(.+)\*")))

(def code
  (parser/map
    (fn [[_ text]]
      (html/->Element "pre" (html/->Element "code" text {}) {}))
    (parser/regex #"^`(.+)`")))

(def inline
  (parser/choice
    anchor
    emphasis
    code
    (parser/regex #"^.+")))

(def header
  (parser/chain
    (fn [header _ text]
      (html/->Element (str "h" (count header)) text {}))
    (parser/regex #"^#+")
    space
    inline))

(defn list-item [size]
  (parser/chain
    (fn [_ _ _ text _]
      (html/->Element "li" text {}))
     (indent size)
     (parser/regex #"^[\*\+\-]")
     space
     inline
     end))

(defn unordered-list [size]
  (parser/chain
    (fn [item items]
      (html/->Element "ul" (vec (cons item items)) {}))
    (list-item size)
    (parser/many (parser/choice (list-item size) (fn [input] ((unordered-list (inc size)) input))))))

(def markdown
  (parser/many
    (parser/choice
      header
      (unordered-list 0)
      inline
      end)))
