(ns scheduler.reductions
  (:require [scheduler.initial-state :refer [initial-state-value]]))


(defn collect [action reductions]
  (get
   reductions
   (:type action)
   identity))

;; todo: write a better custom data abstraction here to hold these that will
;; allow them to be named and exported as a list
(defn- available-times-reduction [action]
  (collect action
           {:add-time (fn [state]
                        (let [time (->> action :data :time)
                              day (->> action :data :day)]
                          (update-in state [day]
                                     (fn [state]
                                       (conj state time)))))
            
            :reset (fn [state] (:available-times initial-state-value))}))

(defn- highlighting-reduction [action]
  (collect action
           {:add-time (fn [state]
                        (merge state {:clicked? false :start nil :end nil}))
            
            :store-starter-timeslot (fn [state]
                                      (merge state {:clicked? true :start (:data action)}))
            
            :reset (fn [state]
                     (update-in state [] (:highlighting initial-state-value)))
            
            :mark-as-selected (fn [state]
                                (update-in state [:end] (fn [_] (:data action))))}))

(defn- reddit-state-reduction [action]
  (collect action
           {:update-reddit-state (fn [state]
                                   (update-in state [:times]
                                              (fn [_]
                                                (:data action))))}))

;;; note: these reduce the top level by themselves
(defn- name-reduction [action]
  (collect action
           {:name (fn [state]
                    (:data action))}))

;; this needs to be processed properly by dispatch!
(def state-update-map {:available-times available-times-reduction
                       :highlighting highlighting-reduction
                       :name name-reduction
                       :reddit reddit-state-reduction})

