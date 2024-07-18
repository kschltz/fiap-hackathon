(ns infra.migrations
  (:require [integrant.core :as ig]
            [xtdb.api :as xt]))

(defmethod ig/init-key ::migrations [_ {:keys [node data] :as cfg}]
  (assoc cfg
    :tx (->> data
             (map #(vector ::xt/put %))
             (into [])
             (xt/submit-tx node))))

(defmethod ig/halt-key! ::migrations [_ {:keys [node data]}]
  (->> data
       (map ::xt/id)
       (map #(vector ::xt/evict %))
       (into [])
       (xt/submit-tx node)))