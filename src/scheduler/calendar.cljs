(ns scheduler.calendar
  (:require [scheduler.actions :as actions]
            [scheduler.datetime :as datetime :refer [Datetime display]]            
            [scheduler.timeslot :refer [timeslot]]
            [scheduler.utility :refer [cx r-map]]
            [scheduler.initial-state :refer [times days-of-the-week]]
            [scheduler.state :refer [app-state]]))

(defn- week-days-row []
    [:tr
     (r-map (fn [day]
              [:th {:key day} (display day)])
            days-of-the-week)])

(defn schedule
  ([state]
   (let [timeslots
         (r-map
          (fn [time]
            [:tr {:key time}
             (r-map (fn [day]
                      (let [datetime (Datetime. day time)
                            highlighting (get-in @state [:highlighting])
                            available-times (get-in @state [:available-times])]
                        (timeslot datetime highlighting available-times)))
                    days-of-the-week)])
          times)]
     [:table {:class (cx "mx-auto" "scheduler-table")}
      [:tbody
       (week-days-row)
       timeslots]]))

  ([state read-only]
   (let [timeslots
         (r-map
          (fn [time]
            [:tr {:key time}
             (r-map (fn [day]
                      (let [datetime (Datetime. day time)
                            available-times (get-in @state [:reddit :times])]
                        (timeslot datetime nil available-times true)))
                    days-of-the-week)])
          times)]
     [:table {:class (cx "mx-auto" "scheduler-table")}
      [:tbody
       (week-days-row)
       timeslots]])))
