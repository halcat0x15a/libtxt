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
      (html/element "p" (html/element "a" text "href" ref)))
    (parser/regex #"^\[(\S+)\]\((\S+)\)")))

(def inline
  (parser/choice
    anchor
    (parser/regex #"^.+")))

(def header
  (parser/chain
    (fn [header _ text]
      (html/element (str "h" (count header)) text))
    (parser/regex #"^#+")
    space
    inline))

(defn list-item [size]
  (parser/chain
    (fn [_ _ _ text _]
      (html/element "li" text))
     (indent size)
     (parser/regex #"^[\*\+\-]")
     space
     inline
     end))

(defn unordered-list [size]
  (parser/chain
    (fn [item items]
      (html/element "ul" (str item (string/join items))))
    (list-item size)
    (parser/many (parser/choice (list-item size) (fn [input] ((unordered-list (inc size)) input))))))

(def markdown
  (parser/choice
    header
    (unordered-list 0)
    inline
    end))

(defn compile [parser]
  (parser/map string/join (parser/many parser)))
