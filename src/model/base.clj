(ns model.base
  (:require [malli.error :as me]
            [buddy.hashers :as hashers]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.transform :as mt]
            [malli.util :as mu]))

(defn hash [x]
  (hashers/derive x))

(def db-transformer
  (mt/transformer
    {:name :db}))


(defn ->db [schema data]
  (m/encode [:and
             [:map {:encode/db
                    (fn [{:xt/keys [id] :as d}]
                      (cond-> d
                              (nil? id) (assoc :xt/id (random-uuid))))}]
             schema]
            data db-transformer))

(defn assert [schema data]
  (if-let [result (m/explain schema data)]
    (throw (ex-info (str "Validation error: "
                         (me/humanize result))
                    {:data   data
                     :result result}))
    data))


(comment
(hash "pass")
  (let [user
        [:or model.paciente/Paciente
         model.medico/Medico]]
    (->> (mg/sample user)
         (map #(->db user %))))

  )