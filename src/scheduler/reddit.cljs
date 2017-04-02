(ns scheduler.reddit
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! >! chan take!]]
            [clojure.string :refer [join split]]
            [goog.string :as gstring]
            goog.string.format
            [scheduler.initial-state :refer [initial-state-value]]
            [scheduler.secrets :as secrets])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn authenticate []
  (let [result (chan)]
    (go (let [response (<! (http/post "https://www.reddit.com/api/v1/access_token"
                                     {:with-credentials? false
                                      :headers {}
                                      :form-params {:grant_type "password"
                                                    :username secrets/reddit-username
                                                    :password secrets/reddit-password}
                                      :basic-auth {:username secrets/reddit-api-app-name
                                                   :password secrets/reddit-api-secret-key}}))]
          (let [data (->> response :body :access_token)]
            ;(prn (str "result data: " data))
            (>! result data))))
    result))

(defn reddit-get
  [endpoint]
  (go
    (let [token (<! (authenticate))
          response (<! (http/get
                        (str "https://oauth.reddit.com" endpoint)
                        {:with-credentials? false
                         :headers {"authorization" (str "bearer " token)}}))]
     response)))

(defn post-comment [parent-chan data]
  (let [c (chan)
        url "https://oauth.reddit.com/api/comment"]
    (go
      (let [parent (<! parent-chan)]
        (http/post url
                  {:oauth-token (<! (authenticate))
                   :with-credentials? false
                   :form-params {:api_type "json"
                                 :text (str data)
                                 :parent parent}})))))


(defn day-of-year [& [a-month a-day]]
  ;; note: doesn't account for leap year
  (let [day-count [0 31 59 90 120 151 181 212 243 273 304 334]
        [month day] (if (and a-month a-day)
                      [a-month a-day]
                      (let [now (js/Date.)]
                        [(.getMonth now)
                         (.getDate now)]))]    
    (+ day (get day-count month))))

(defn too-old? [month day]
  (let [today (day-of-year)
        other (day-of-year (dec month) day)]
    (< 6 (- today other))))

(defn previous-sunday []
  (let [date (js/Date.)
        offset (* 1000 60 60 24 (.getDay date))
        previous-sunday (js/Date. (.setTime date (- (.getTime date) offset)))]
    (str (+ 1900 (.getYear previous-sunday))
         "-"
         (gstring/format "%02f" (inc (.getMonth
                                     previous-sunday)))
         "-"
         (gstring/format "%02f" (.getDate previous-sunday)))))

(defn create-new-entry []
  (go
    (let [c (chan)
          content (previous-sunday) ]
      (prn "Creating a new reddit thread")
      (http/post "https://oauth.reddit.com/api/submit"
                 {:oauth-token (<! (authenticate))
                  :with-credentials? false
                  :form-params {:api_type "json"
                                :sr "dnd_Schedule"
                                :kind "self"
                                :title (str content)
                                :text (str "schedule for week of " content)
                                :parent "t5_3jka4"}}))))


(defn get-most-recent-parent-id
  "Returns a channel with the url in it"
  []
  (go
    (let [c (chan)
          id-chan (chan)
          reddit-data (<! (reddit-get "/r/dnd_Schedule"))]
      (let [data  (->> reddit-data
                       :body
                       :data
                       :children
                       first
                       :data)
            [year month day] (map js/parseInt
                                  (clojure.string/split (:title data) "-"))
            id (:name data)]
        (prn (str month day))
        (if (too-old? month day)
          (do 
            (create-new-entry)
            (get-most-recent-parent-id))
          id)))))

(defn strip-reddit-base [url]
  (->> "/"
       (split url)
       (drop 3)
       (join "/")
       (str "/")))


(defn get-comments [id-chan]
  (go
    (let [result (chan)
          id (<! id-chan)

          endpoint (->> "_"
                        (split id)
                        second
                        (str "/r/dnd_Schedule/comments/"))
          map-children #(map
                         (fn [comment-response]
                           (->> comment-response
                                :data
                                :body
                                cljs.reader/read-string)) %)
          reddit-data (<! (reddit-get endpoint))]

      (->>  reddit-data
            :body
            second
            :data
            :children
            map-children))))

(defn schedule-merge [schedule-1 schedule-2]
  (reduce (fn [acc [day time-set]]
            (update-in acc [day] (fn [acc-times]
                                   (clojure.set/intersection acc-times time-set))))
          schedule-1
          schedule-2))


(defn get-mutual-schedule [comment-chan]
  (go
    (let [comments (<! comment-chan)]
      (reduce (fn [acc weekly-schedule]
                (schedule-merge weekly-schedule acc))
              (map :available-times comments)))))

(defn get-schedule []
  (->> (get-most-recent-parent-id)
       get-comments
       get-mutual-schedule))
