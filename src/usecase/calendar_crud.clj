(ns usecase.calendar-crud
  (:require [model.base :as base]
            [model.calendar :as calendar]
            [xtdb.api :as xt]))

(defn create-calendar [node calendar]
  (->> (calendar/assert-calendar calendar)
       (base/->db calendar/Calendar)
       (vector ::xt/put)
       (into [])
       (xt/submit-tx node)))

(comment

  (create-calendar node )

  )
