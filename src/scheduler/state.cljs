(ns scheduler.state
  (:require [reagent.core :refer [atom]]
            [scheduler.initial-state :refer [initial-state-value]]
            [scheduler.reductions :refer [state-update-map]]))

(def app-state (atom initial-state-value))
(def time-to-add (scheduler.datetime/Datetime. 11 6))

(defn collect [action reductions]
  (get
   reductions
   (:type action)
   identity))

(defn dispatch [action]
  (doseq [[state-slice reduction] state-update-map]
    (swap! app-state
           (fn [state]
             (update-in state [state-slice]
                        (reduction action))))))
