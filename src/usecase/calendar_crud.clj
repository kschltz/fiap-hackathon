(ns usecase.calendar-crud
  (:require [model.base :as base]
            [model.calendar :as calendar]
            [xtdb.api :as xt])
  (:import (java.time LocalTime)))

(defn create-calendar [node calendar]
  (->> (calendar/assert-calendar calendar)
       (base/->db calendar/Calendar)
       (vector ::xt/put)
       vector
       (xt/submit-tx node)))


(defn update-calendar [node calendar]
  (base/assert [:map [:id {:error-message "ID obrigatório"} :string]] calendar)
  (let [id (:id calendar)
        old-data (xt/entity (xt/db node) id)
        to-insert (merge old-data calendar)]
    (create-calendar node to-insert)))

#_(defn slot-available? [availabilities time]
  (some #(and (= (:from %) time) (not (:booked %))) availabilities))

(defn get-calendar [node medic-id date]
  (xt/entity (xt/db node) (str date "#" medic-id)))

(defn book-appointment [node medic-id date time patient-id]
  (let [calendar (get-calendar node medic-id date)
        updated-availabilities (map (fn [slot]
                                      (if (and (= (:from slot) time) (not (:booked slot)))
                                        (assoc slot :booked true :patient-id patient-id)
                                        slot))
                                    (:availabilities calendar))
        updated-calendar (assoc calendar :availabilities updated-availabilities)]
    (update-calendar node updated-calendar)))

(defn unbook-appointment [node medic-id date time]
  (let [calendar (get-calendar node medic-id date)
        updated-availabilities (map (fn [slot]
                                      (if (and (= (:from slot) time) (:booked slot))
                                        (dissoc slot :booked :patient-id)
                                        slot))
                                    (:availabilities calendar))
        updated-calendar (assoc calendar :availabilities updated-availabilities)]
    (update-calendar node updated-calendar)))

(comment
  (malli.generator/sample calendar/Calendar)

  (def c {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
          :year           2024
          :month          9
          :day            19
          :availabilities [{:from (LocalTime/of 8 0)
                            :to   (LocalTime/of 12 0)}]})
  (base/->db calendar/Calendar c)
  (create-calendar (user/node) c)

  (update-calendar
    (user/node)
    (-> c
        (update :availabilities
                #(conj % {:from (LocalTime/of 14 0)
                          :to   (LocalTime/of 19 0)}))))

  (xt/q (user/db) '{:find  [(pull e [*])]
                    :where [[e :xt/type :calendar]]})

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
