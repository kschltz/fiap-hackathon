(ns infra.http.routes.medic
  (:require
    [clojure.data.json :as json]
    [infra.http.server :as server]
    [model.appointment :as appointment]
    [model.base :as base]
    [model.calendar :as calendar]
    [usecase.appointment :as uc.appointment]
    [usecase.calendar-crud :as uc.calendar-crud]
    [usecase.medic-search :as uc.medic-search])
  (:import
    (java.time
      LocalTime)))


(defn search-medic
  [{:keys [app query-params]}]
  (try
    (let [{:keys [xtdb]} app
          especialidade (:especialidade query-params)
          result (uc.medic-search/search-medic xtdb especialidade)
          medics (map (fn [medic] (dissoc medic :senha)) result)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body   {:medics medics}})
    (catch Exception e
      {:status  500
       :headers {"Content-Type" "application/json"}
       :body    (.getMessage e)})))


(defn save-calendar
  [{:keys [app json-params user]}]
  (try
    (let [{:strs [id]} user
          {:keys [xtdb]} app
          calendar (-> (base/json-> calendar/Calendar json-params)
                       (assoc-in [:medic-id] (parse-uuid id)))]
      (if (uc.calendar-crud/create-calendar xtdb calendar)
        {:status 200}
        {:status 400}))
    (catch Exception e
      {:status  500
       :headers {"Content-Type" "application/json"}
       :body    (.getMessage e)})))


(defn update-calendar
  [{:keys [app json-params user]}]
  (try
    (let [{:strs [id]} user
          {:keys [xtdb]} app
          calendar (-> (base/json-> calendar/Calendar json-params)
                       (assoc-in [:medic-id] (parse-uuid id)))]
      (if (uc.calendar-crud/update-calendar xtdb calendar)
        {:status 200}
        {:status 400}))
    (catch Exception e
      {:status  500
       :headers {"Content-Type" "application/json"}
       :body    (.getMessage e)})))

(defn manage-appointment [{:keys [app json-params]}]
  (try
    (let [{:keys [xtdb]} app
          {:keys [appointment-id action]} (base/json-> appointment/ManageAppointment json-params)]
      (case action
        "accept" (uc.appointment/accept-appointment xtdb appointment-id)
        "reject" (uc.appointment/reject-appointment xtdb appointment-id))
      {:status 200 :body {:message "Appointment managed successfully"}})
    (catch Exception e
      {:status 500
       :headers {"Content-Type" "application/json"}
       :body (.getMessage e)})))

(def routes
  ["/medic" ^:interceptors [server/authenticate-interceptor]
   {:get `search-medic}
   ["/calendar" ^:interceptors [(server/type-exclusive-interceptor "medico")]
    {:post `save-calendar
     :put  `update-calendar}]
   ["/appointment" ^:interceptors [(server/type-exclusive-interceptor "medico")]
    {:put `manage-appointment}]])
