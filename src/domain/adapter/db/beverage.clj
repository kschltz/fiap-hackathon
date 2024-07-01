(ns domain.adapter.db.beverage
  (:require [domain.model.beverage :refer [assert beverage-domain]]
            [xtdb.api :as xt]))

(defn insert [db beverage & bvgs]

  (let [beverages (->> (into [beverage] bvgs)
                       (map #(assoc % :xt/type beverage-domain)))
        trxs (into [] (map #(vector ::xt/put %)) beverages)]
    (every? assert beverages)
    (xt/submit-tx db trxs)))

(defn list [db & clauses]
  (->>
    (doto {:find '[(pull e [*])]
           :where
           (into [['e :xt/type beverage-domain]] clauses)} tap>)
    (xt/q db)
    (map first)))
