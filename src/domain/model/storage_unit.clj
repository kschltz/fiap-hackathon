(ns domain.model.storage-unit
  (:require [domain.model.validation :as validation]))

(def StorageUnitId pos-int?)

(def storage-unit-domain
  ::storage-unit)

(def StorageUnit
  (let [a-capacity 500
        na-capacity 400
        usage [:map
               {:gen/fmap (fn [u]
                            (assoc u (rand-nth [:alcoholic :non-alcoholic]) 0))}
               [:alcoholic [:int {:min 0 :max a-capacity}]]
               [:non-alcoholic [:int {:min 0 :max na-capacity}]]]]
    [:map
     [:xt/id StorageUnitId]
     [:xt/type [:= storage-unit-domain]]
     [:name :string]
     [:capacity [:map
                 [:alcoholic [:= a-capacity]]
                 [:non-alcoholic [:= na-capacity]]]]
     [:usage [:and
              usage
              [:fn  {:error/message "You cannot mix beverage types"}
               (fn [{:keys [alcoholic non-alcoholic]}]
                     (boolean
                       (not (and (pos-int? alcoholic)
                                 (pos-int? non-alcoholic)))))]]]]))

(defn assert [data]
  (validation/assert StorageUnit data))