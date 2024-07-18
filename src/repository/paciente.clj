(ns repository.paciente
  (:require [model.base :as base]
            [xtdb.api :as xt]
            [model.paciente :as paciente]))

(defn persist [node & data]
  (->> (map paciente/assert-paciente data)
       (map #(base/->db paciente/Paciente %))
       (map #(vector ::xt/put %))
       vec
       (xt/submit-tx node)))
