(ns txtlib.core.parser.plain
  (:require [clojure.string :as string]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.parser :as parser]
            [txtlib.core.format :as format]))

(def cursor
  (parser/chain
   (fn [_ value] (format/->Label :cursor value))
   (parser/parser (re-pattern (str \^ format/*cursor*)))
   (parser/parser #"[\s\S]")))

(def selection
  (parser/chain
   (fn [_ value _] (format/->Label :selection value))
   (parser/parser (re-pattern (str \^ format/*start*)))
   (parser/many
    (parser/choice
     cursor
     (parser/parser (re-pattern (str "^[^" format/*end* "]")))))
   (parser/parser (re-pattern (str \^ format/*end*)))))

(def parser
  (parser/many
   (parser/choice
    cursor
    selection
    (parser/parser #"[\s\S]"))))

(defn parse [buffer]
  (-> buffer format/show parser :value))
