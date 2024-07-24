(ns model.base
  (:require [buddy.hashers :as hashers]
            [malli.core :as m]
            [malli.error :as me]
            [malli.transform :as mt]))

(defn hash [x]
  (hashers/derive x))

(defn hashed-compared [x hashed]
  (hashers/check x hashed))

(def db-transformer
  (mt/transformer
    {:name :db}))

(def json-transformer
  (mt/transformer
    {:name :custom-json}))

(defn json-> [schema data]
  (m/decode schema data json-transformer))

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
