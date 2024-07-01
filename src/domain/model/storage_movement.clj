(ns domain.model.storage-movement
  (:require [domain.model.storage-unit :refer [StorageUnitId]]
            [domain.model.validation :as validation]
            [domain.model.beverage :refer [BeverageId]]
            [malli.util :as mu]))

(def MovementId :uuid)

(def MovementType
  [:enum "in" "out"])

(def domain
  ::movement)

(def MovementMetadata
  [:map
   [:xt/id MovementId]
   [:xt/type [:= domain]]
   [:time inst?]
   [:type MovementType]
   [:beverage BeverageId]
   [:employee :string]
   [:storage-unit StorageUnitId]
   [:quantity pos-int?]])

(def Deliver
  (mu/merge
    MovementMetadata
    [:map [:type [:= "out"]]]))

(def Receive
  (mu/merge
    MovementMetadata
    [:map [:type [:= "in"]]]))

(def StorageMovement
  [:or Deliver Receive])

(defn assert [data]
  (validation/assert StorageMovement data))
