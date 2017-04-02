(ns scheduler.datetime)

(defn- get-max-or-min [max-or-min this other]
  [(max-or-min (first this) (first other))
   (max-or-min (second this) (second other))])

(defprotocol Identifiable
  "Makes an entity identifiable as parts or as a whole"
  (identify [this] "Identify an object uniquely")
  (identify-indices [this] "Identify as a vector of its constituent parts"))

(defprotocol Normalize
  (normalize [this other]))

(defprotocol Internals
  (get-inside [this other]))

(defrecord Datetime [day time]
  
  Identifiable
  (identify [this] (str day time))
  (identify-indices [this] [day time])
  
  Normalize
  (normalize [this other]
    (let [a (identify-indices this)
          b (identify-indices other)
          start (get-max-or-min min a b)
          end (get-max-or-min max a b)]
      [(Datetime. (first start) (second start)) (Datetime. (first end) (second end))])))



(defn display
  "Display my times and dates appropriately."
    [elm]
  (cond
    (< elm 11) (str elm ":00 PM")
    true (condp = elm    
           11 "Monday"
           12 "Tuesday"
           13 "Wednesday"
           14 "Thursday"
           15 "Friday"
           16 "saturday"
           17 "Sunday")))
