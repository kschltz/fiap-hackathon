(ns domain.adapter.db.storage
  (:require [xtdb.api :as xt]
            [domain.model.storage-movement :as movement]
            [domain.model.storage-unit :refer [storage-unit-domain assert]]))

(defn insert [db storage]
  (assert storage)
  (xt/submit-tx db [[::xt/put storage]]))

(defn list [db & clauses]
  (->> (xt/q db {:find '[(pull e [*])]
                 :where
                 (into [['e :xt/type storage-unit-domain]] clauses)})
       (map first)))

(defn movement-log [db & {:keys [order clauses]
                          :or   {order '[[n]
                                         [t :desc]]}}]
  (->> (xt/q db {:find     '[(pull e [*]) n t su]
                 :order-by order
                 :where
                 (into '[[e :xt/type movement/domain]
                         [e :time t]
                         [e :beverage bev]
                         [e :storage-unit su]
                         [su :name n]] clauses)})
       (map first)))
