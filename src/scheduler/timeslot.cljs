(ns scheduler.timeslot
  (:require [scheduler.actions :as actions]
            [scheduler.datetime :refer [Datetime display normalize]]
            [scheduler.utility :refer [cx]]
            [scheduler.datetime :refer [identify]]
            [scheduler.reddit :as reddit]))

(defn- time-range [start end]
  (let [times (into #{} (range (:time start) (inc (:time end))))
        days (range (:day start) (inc (:day end)))]
    (reduce (fn [acc day]
              (assoc acc day times))
            {}
            days)))

(defn- add-scheduled-times [end-datetime highlighting]
  (let [initial-datetime (get-in highlighting [:start])
        [start end] (normalize end-datetime initial-datetime)]
    (actions/add-times (time-range start end))))

(defn- store-start-corner [datetime]
  (actions/store-starter-timeslot datetime))

(defn- under-cursor-square? [datetime highlighting]
  (let [start (:start highlighting)
        end (:end highlighting)]
    (if (some #(= % nil) [start end])
      false

      (reduce (fn [contained? [day times]]
                (or contained?
                    (and (= day (:day datetime))
                         (contains? times (:time datetime)))))
              false
              (apply time-range (normalize start end))))))

(defn- selected? [datetime available-times highlighting]
  (let [times-in-day (get available-times (:day datetime))]    
    (or
     (under-cursor-square? datetime highlighting)
     (contains? times-in-day (:time datetime)))))

(defn timeslot [datetime highlighting available-times & read-only]
  (let [day (:day datetime)
        time (:time datetime)
        class (if (selected? datetime available-times highlighting)
                "selected"
                (if-not read-only
                  "not-selected"
                  "not-selected-read-only"))]
    [:td {:class (cx "timeslot" "unselectable-text" class)
          :on-mouse-down (fn [] (if-not read-only
                                  (store-start-corner datetime)))
          :on-mouse-up (fn [_] (if-not read-only
                                 (add-scheduled-times datetime highlighting)))
          :on-mouse-over (fn [_] (if-not read-only
                                   (actions/mark-as-selected datetime)))
          :key (int (str (identify datetime)))}
     (display time)]))
