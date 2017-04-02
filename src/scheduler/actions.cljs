(ns scheduler.actions
  (:require [scheduler.datetime :refer [Datetime]]
            [scheduler.reddit :refer [post-comment authenticate get-most-recent-parent-id get-schedule]]
            [scheduler.state :as state :refer [dispatch]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; These are actions that'll be sent to a store to update the app-state

(defn add-time
  ([day time]
   (dispatch {:type :add-time
              :data (Datetime. day time)}))
  ([datetime]
   (dispatch {:type :add-time
              :data datetime})))

(defn add-selected-area [date-time-map]
  ;; TODO: defrecord date-time-map
  )

(defn reset-state []
  (dispatch {:type :reset}))

(defn store-starter-timeslot [datetime]
  (dispatch {:type :store-starter-timeslot
             :data datetime}))

(defn add-times [date-time-map]
  (doseq [[day times] date-time-map]
    (doseq [time times]
      (add-time day time))))

(declare load-reddit-state)

(defn mark-as-selected [datetime]
  (dispatch {:type :mark-as-selected
             :data datetime}))
(defn save-name [e]
  (let [name (-> e .-target .-value)]
    (dispatch {:type :name
               :data name})))

(defn load-reddit-state []
  (go
    (let [reddit-state (<! (get-schedule))]
      (dispatch {:type :update-reddit-state
                 :data reddit-state}))))

(defn save [_]
  (let [fields-to-send #{:name :available-times}
        state-to-save (reduce (fn [acc [k v]]
                                (if (contains? fields-to-send k)
                                  (assoc acc k v)
                                  acc))
                              {}
                              @state/app-state)]
    (go
      (post-comment (get-most-recent-parent-id) state-to-save)))
  (reset-state)
  (load-reddit-state))
