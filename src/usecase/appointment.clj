(ns usecase.appointment
  (:require [model.appointment :as appointment]
            [model.base :as base]
            [xtdb.api :as xt]
            [usecase.calendar-crud :as uc.calendar-crud]))

(defn create-appointment [xtdb {:keys [medic-id date time patient-id]}]
  (let [appointment {:medic-id medic-id
                     :date date
                     :time time
                     :patient-id patient-id
                     :status :pending}]
    (->> appointment
         (base/->db appointment/Appointment)
         (vector ::xt/put)
         vector
         (xt/submit-tx xtdb))))

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
