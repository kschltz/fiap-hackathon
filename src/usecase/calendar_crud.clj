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
  (let [{:xt/keys [id] :as new-data} (base/->db calendar/Calendar calendar)
        old-data (xt/entity (xt/db node) id)
        to-insert (merge old-data new-data)]
    (create-calendar node calendar)))

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
                    :where [[e :xt/type :calendar]]}))
