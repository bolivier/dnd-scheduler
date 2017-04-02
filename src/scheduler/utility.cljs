(ns scheduler.utility
  (:require [clojure.string :refer [join]]))


(defn cx [& classes]
  (join " " classes))

(defn r-map [f coll]
  (doall
   (map f coll)))
