(ns domain.usecase.storage
  (:require [domain.model.storage-unit :as storage-unit]
            [domain.model.storage-movement :as storage-movement]
            [domain.model.validation :as validation]
            [domain.model.beverage :as beverage])
  (:import (java.util Date)))


(defn store-beverage
  [storage-unit
   {:xt/keys [id] bev-type :type :as beverage}
   liters
   employee
   alcohol-in-last-day?]

  (storage-unit/assert storage-unit)
  (beverage/assert beverage)
  (validation/assert pos-int? liters)
  (when (and alcohol-in-last-day?
             (= "non-alcoholic" bev-type))
    (throw
      (ex-info
        "Can't store non-alcoholic beverage due to recent alcoholic volume stored"
        {:type bev-type})))

  (let [storage-movement {:xt/id        (random-uuid)
                          :xt/type      storage-movement/domain
                          :time         (Date.)
                          :type         "in"
                          :beverage     id
                          :employee     employee
                          :storage-unit (:xt/id storage-unit)
                          :quantity     liters}

        storage-unit (update-in storage-unit [:usage (keyword bev-type)] + liters)]
    (storage-movement/assert storage-movement)
    (storage-unit/assert storage-unit)

    (doto {:storage-unit     storage-unit
           :storage-movement storage-movement}
      tap>)))



