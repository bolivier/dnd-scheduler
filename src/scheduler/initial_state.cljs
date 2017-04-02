(ns scheduler.initial-state)

(def days-of-the-week [11 12 13 14 15 16 17]) 
(def times [6 7 8 9 10])
(def initial-state-value {:available-times (reduce (fn [acc day]
                                                     (assoc acc day #{}))
                                                   {}
                                                   days-of-the-week)
                          :name ""
                          :reddit {:times nil
                                   :latest-thread nil
                                   :token nil}
                          :highlighting {:clicked? false
                                         :start nil
                                         :end nil}})

