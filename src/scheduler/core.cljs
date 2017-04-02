(ns scheduler.core
  (:require [clojure.string :refer [join]]
            [reagent.core :as reagent]
            [scheduler.actions :as actions]
            [scheduler.calendar :refer [schedule]]
            [scheduler.state :refer [app-state]]))

(enable-console-print!)

(println "This text is printed from src/scheduler/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defn app []
  [:div.container
   [:div.mx-auto {:style {:margin-top "25px"}}
    [:h1 "D&D Scheduler"]]
   [:div.row
    [:div.col-md-4 {:style {:margin-top "15%" }}
     [:div.container
      [:p "Name: "]
      [:input {:on-change actions/save-name
               :placeholder "Who you be?"}]
      [:br]
      [:button.spaced {:on-click actions/reset-state} "reset"]
      [:button.spaced {:on-click  actions/save} "save"]]]
    [:div.col-md-8.top-margin (schedule app-state)]]
   [:h3 {:style {:text-align "center"
                 :margin-top "70px"}}
    "Group Schedule"]
   [:div.mx-auto {:style {:padding-bottom "50px"}}
    (schedule app-state :read-only)]
   [:h6.fine-print
    "Created for the DnD Fun Group by the Glorious USSDR (United Soviet Socialist Dwarven Republic)"]
   ])

(actions/load-reddit-state)
(reagent/render-component [app]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
