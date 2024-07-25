(ns infra.http.routes.medic
  (:require [clojure.data.json :as json]
            [infra.http.server :as server]
            [model.base :as base]
            [model.calendar :as calendar]
            [usecase.calendar-crud :as uc.calendar-crud]
            [usecase.medic-search :as uc.medic-search])
  (:import (java.time LocalTime)))

(defn search-medic [{:keys [app query-params]}]
  (try
    (let [{:keys [xtdb]} app
          especialidade (:especialidade query-params)
          medics (uc.medic-search/search-medic xtdb especialidade)]
      {:status 200
       :body   {:medics medics}})
    (catch Exception e
      {:status  500
       :headers {"Content-Type" "application/json"}
       :body    (.getMessage e)})))

(defn save-calendar [{:keys [app json-params user]}]
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

(defn update-calendar [{:keys [app json-params user]}]
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

(defn schedule-appointment [{:keys [app json-params user]}]
  (try
    (let [{:strs [id]} user
          {:keys [xtdb]} app
          {:keys [medic-id date time]} json-params
          calendar (uc.calendar-crud/get-calendar xtdb medic-id date)
          slot-available? (uc.calendar-crud/slot-available? (:availabilities calendar) time)]
      (if (and calendar slot-available?)
        (do
          (uc.calendar-crud/book-appointment xtdb medic-id date time id)
          {:status 200 :body {:message "Appointment scheduled successfully"}})
        {:status 400 :body {:message "Time slot not available"}}))
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
   ["/appointment" ^:interceptors [server/authenticate-interceptor]
    {:post `schedule-appointment}]])


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
