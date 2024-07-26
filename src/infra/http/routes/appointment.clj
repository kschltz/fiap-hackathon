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
  ["/patient/appointment" ^:interceptors [server/authenticate-interceptor
                                          (server/type-exclusive-interceptor "paciente")]
   {:post `schedule-appointment
    :put  `delete-appointment}
   "/medic/manage-appointment" ^:interceptors [server/authenticate-interceptor
                                               (server/type-exclusive-interceptor "medico")]
   {:put `manage-appointment}])

(comment



  (hato.client/get
    "http://localhost:8080/medic?especialidade=oftalmologia"
    {:content-type :json
     :headers      {"Authorization"
                    "Bearer eyJhbGciOiJIUzI1NiJ9.IntcIm5vbWVcIjpcIkRlbnRpc3RvXCIsXCJlc3BlY2lhbGlkYWRlXCI6XCJvZG9udG9sb2dpYVwiLFwidHlwZVwiOlwibWVkaWNvXCIsXCJjcm1cIjpcIjY1NDUzNi00NC1TUFwiLFwic2VuaGFcIjpcImJjcnlwdCtzaGE1MTIkMTMyMDg0MzcxN2ZmMTc3OWY1ZDVjNTZiNjYxMjZmYmMkMTIkMTFiMjRiNmE3ZmEwY2Q2MTE5NzlmNDM3NGNmZGY4ZWY2Zjg0NzQwNzhiMTY0YWYyXCIsXCJpZFwiOlwiYzNlNDdjMjktMzFiNC00ZTQ5LWE0MDgtMTc5NzcyODIzZjdjXCIsXCJleHBpcmVzLWF0XCI6XCIyMDI0LTA3LTIzVDIxOjMxOjUwLjU5MzU5NjgzMlwifSI.KFxu_kcBo8Dmalwn-XXp64zoVg5b7YQppn6dOdj0o7M"}})


  (hato.client/post
    "http://localhost:8080/medic/calendar"
    {:content-type :json
     :headers
     {"Authorization"
      "Bearer eyJhbGciOiJIUzI1NiJ9.IntcIm5vbWVcIjpcIkRlbnRpc3RvXCIsXCJlc3BlY2lhbGlkYWRlXCI6XCJvZG9udG9sb2dpYVwiLFwidHlwZVwiOlwibWVkaWNvXCIsXCJjcm1cIjpcIjY1NDUzNi00NC1TUFwiLFwic2VuaGFcIjpcImJjcnlwdCtzaGE1MTIkMTMyMDg0MzcxN2ZmMTc3OWY1ZDVjNTZiNjYxMjZmYmMkMTIkMTFiMjRiNmE3ZmEwY2Q2MTE5NzlmNDM3NGNmZGY4ZWY2Zjg0NzQwNzhiMTY0YWYyXCIsXCJpZFwiOlwiYzNlNDdjMjktMzFiNC00ZTQ5LWE0MDgtMTc5NzcyODIzZjdjXCIsXCJleHBpcmVzLWF0XCI6XCIyMDI0LTA3LTIzVDIxOjMxOjUwLjU5MzU5NjgzMlwifSI.KFxu_kcBo8Dmalwn-XXp64zoVg5b7YQppn6dOdj0o7M"}
     :body         (json/write-str
                     {:year           2024
                      :month          9
                      :day            19
                      :availabilities [{:from (str (LocalTime/of 8 0))
                                        :to   (str (LocalTime/of 12 0))}]})})


  (hato.client/put
    "http://localhost:8080/medic/calendar"
    {:content-type :json
     :headers
     {"Authorization"
      "Bearer eyJhbGciOiJIUzI1NiJ9.IntcIm5vbWVcIjpcIkRlbnRpc3RvXCIsXCJlc3BlY2lhbGlkYWRlXCI6XCJvZG9udG9sb2dpYVwiLFwidHlwZVwiOlwibWVkaWNvXCIsXCJjcm1cIjpcIjY1NDUzNi00NC1TUFwiLFwic2VuaGFcIjpcImJjcnlwdCtzaGE1MTIkMTMyMDg0MzcxN2ZmMTc3OWY1ZDVjNTZiNjYxMjZmYmMkMTIkMTFiMjRiNmE3ZmEwY2Q2MTE5NzlmNDM3NGNmZGY4ZWY2Zjg0NzQwNzhiMTY0YWYyXCIsXCJpZFwiOlwiYzNlNDdjMjktMzFiNC00ZTQ5LWE0MDgtMTc5NzcyODIzZjdjXCIsXCJleHBpcmVzLWF0XCI6XCIyMDI0LTA3LTIzVDIxOjMxOjUwLjU5MzU5NjgzMlwifSI.KFxu_kcBo8Dmalwn-XXp64zoVg5b7YQppn6dOdj0o7M"}
     :body         (json/write-str
                     {:id   "2024-9-19#c3e47c29-31b4-4e49-a408-179772823f7c"
                      :availabilities [{:from (str (LocalTime/of 8 0))
                                        :to   (str (LocalTime/of 15 0))}]})})

  )

