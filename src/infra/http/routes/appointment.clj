(ns infra.http.routes.appointment
  (:require [infra.http.server :as server]
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

(def routes
  ["/patient" ^:interceptors [server/authenticate-interceptor]
   ["/appointment" ^:interceptors [(server/type-exclusive-interceptor "paciente")]
    {:post `schedule-appointment
     :put  `delete-appointment}]])