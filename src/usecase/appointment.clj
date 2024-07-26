(ns usecase.appointment
  (:require [xtdb.api :as xt]
            [usecase.calendar-crud :as uc.calendar-crud]))

(defn create-appointment [xtdb {:keys [medic-id date time patient-id]}]
  (let [appointment-id (str date "#" medic-id "#" time "#" patient-id)
        appointment {:xt/id appointment-id
                     :medic-id medic-id
                     :date date
                     :time time
                     :patient-id patient-id
                     :status :pending}]
    (xt/submit-tx xtdb [[::xt/put appointment]])
    appointment-id))

(defn cancel-appointment [xtdb {:keys [medic-id date time patient-id]}]
  (let [db (xt/db xtdb)
        appointment-id (str date "#" medic-id "#" time "#" patient-id)
        appointment (xt/entity db appointment-id)]
    (if (and appointment (= (:patient-id appointment) patient-id))
      (do
        (xt/submit-tx xtdb [[::xt/put (assoc appointment :status :canceled)]])
        (uc.calendar-crud/unbook-appointment xtdb medic-id date time)
        {:status :success :message "Appointment canceled successfully"})
      {:status :error :message "Appointment not found or user not authorized"})))

(defn accept-appointment [xtdb appointment-id]
  (let [db (xt/db xtdb)
        appointment (xt/entity db appointment-id)]
    (when (= (:status appointment) :pending)
      (xt/submit-tx xtdb [[::xt/put (assoc appointment :status :accepted)]])
      (uc.calendar-crud/book-appointment xtdb (:medic-id appointment) (:date appointment) (:time appointment) (:patient-id appointment)))))

(defn reject-appointment [xtdb appointment-id]
  (let [db (xt/db xtdb)
        appointment (xt/entity db appointment-id)]
    (when (= (:status appointment) :pending)
      (xt/submit-tx xtdb [[::xt/put (assoc appointment :status :rejected)]]))))


(comment

  (def medic-calendar-example
    {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
     :year           2024
     :month          9
     :day            19
     :availabilities [{:from "08:00" :to "09:00" :booked? false}
                      {:from "09:00" :to "10:00" :booked? false}
                      {:from "10:00" :to "11:00" :booked? true :patient-id #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"}
                      {:from "12:00" :to "13:00" :booked? false}]})

  (def request-appointment-example
    {:medic-id #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
     :date     "2024-09-19"
     :time     "11:00"})

  )