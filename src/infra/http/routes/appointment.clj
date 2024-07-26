(ns infra.http.routes.appointment
  (:require [clojure.data.json :as json]
            [infra.http.server :as server]
            [model.base :as base]
            [model.appointment :as appointment]
            [usecase.appointment :as uc.appointment]))

(defn schedule-appointment [{:keys [app json-params user]}]
  (try
    (let [{:strs [id]} user
          {:keys [xtdb]} app
          appointment (-> (base/json-> appointment/Appointment json-params)
                                           (assoc :patient-id id))]

      (uc.appointment/create-appointment xtdb appointment)
      {:status 200 :body {:message "Appointment requested successfully"}})
    (catch Exception e
      {:status 500
       :headers {"Content-Type" "application/json"}
       :body (.getMessage e)})))

(defn delete-appointment [{:keys [app json-params user]}]
  (try
    (let [{:strs [id]} user
          {:keys [xtdb]} app
          appointment (-> (base/json-> appointment/Appointment json-params)
                          (assoc :patient-id id))
          result (uc.appointment/cancel-appointment xtdb appointment)]
      (if (= :success (:status result))
        {:status 200 :body {:message "Appointment deleted successfully"}}
        {:status 400 :body {:message "Appointment not found or user not authorized"}}))
    (catch Exception e
      {:status 500
       :headers {"Content-Type" "application/json"}
       :body (.getMessage e)})))

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
  [["/patient/appointment" ^:interceptors [(server/type-exclusive-interceptor "paciente")]
    {:post `schedule-appointment
     :put  `delete-appointment}]
   ["/medic/manage-appointment" ^:interceptors [(server/type-exclusive-interceptor "medico")]
    {:put `manage-appointment}]])

(comment
  )
