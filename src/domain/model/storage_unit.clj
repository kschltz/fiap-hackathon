(ns domain.model.storage-unit)

(def StorageUnitId pos-int?)

(def StorageUnit
  [:map
   [:id StorageUnitId]
   [:name :string]
   [:capacity [:map
               [:alcoholic [:= 500]]
               [:non-alcoholic [:= 400]]]]])
