(ns txtlib.core.parser.clojure
  (:refer-clojure :exclude [keyword comment])
  (:require [txtlib.core.parser :as parser]))

(defrecord Label [name value])

(def definition
  (parser/chain
   (fn [def space name]
     [(Label. :special def) space (Label. :symbol name)])
   (parser/parser #"^def\w*")
   (parser/parser #"^\s+")
   (parser/parser #"^\w+")))

(def special
  (parser/chain
   (fn [special _] (Label. :special special))
   (parser/parser #"^(def\w*|if|do|let|quote|var|fn|loop|recur|throw|try)")
   (parser/not #"^\w")))

(defn string [input]
  (parser/map
   (parser/parse #"^\"[\s\S]*?\"" input)
   (partial ->Label :string)))

(defn keyword [input]
  (parser/map
   (parser/parse #"^:[^\(\)\[\]\{\}\s]+" input)
   (partial ->Label :keyword)))

(defn comment [input]
  (parser/map
   (parser/parse #"^;.*" input)
   (partial ->Label :comment)))

(def expression
  (parser/many
   (parser/choice
    definition
    special
    string
    keyword
    comment
    (parser/parser #"^[\s\S]"))))
