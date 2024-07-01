(ns domain.adapter.db.prefill
  (:require [domain.adapter.db.beverage :as db.bev]
            [domain.adapter.db.storage :as db.sto]
            [domain.model.storage-unit :refer [StorageUnit]]
            [integrant.core :as ig]
            [xtdb.api :as xt]
            [malli.generator :as mg]))

(defmethod ig/init-key ::prefill
  [_ {:keys [db]}]

  (let [beverages [{:xt/id       (random-uuid)
                    :xt/type     :domain.model.beverage/beverage,
                    :name        "Diet Coke",
                    :type        "non-alcoholic",
                    :price-cents 2500}
                   {:xt/id       (random-uuid)
                    :xt/type     :domain.model.beverage/beverage,
                    :name        "Whisky",
                    :type        "alcoholic",
                    :price-cents 20000}]

        add-storage (fn [i]
                      (let [s (merge (mg/generate StorageUnit)
                                     {:xt/id (inc i)
                                      :usage {:alcoholic 0 :non-alcoholic 0}
                                      :name  (str "ST0" i)})]
                        (db.sto/insert db s)
                        s))]
    (tap> (apply db.bev/insert db beverages))
    {:db            db
     :beverages     beverages
     :storage-units (reduce (fn [acc i] (conj acc (add-storage i))) [] (range 5))}))


(defmethod ig/halt-key! ::prefill
  [_ {:keys [db beverages storage-units]}]
  (->> (concat beverages storage-units)
       (map :xt/id)
       (map #(vector ::xt/evict %))
       (into [])
       (xt/submit-tx db)))