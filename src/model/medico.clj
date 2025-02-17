(ns model.medico
  (:require [malli.generator :as mg]
            [value-object.crm :as crm]
            [model.base :as base]))

(def Medico
  [:map {:encode/db (fn [x] (assoc x :xt/type :medico))}
   [:nome :string]
   [:especialidade :string]
   [:senha {:encode/db base/hash} :string]
   [:crm crm/CRM]])

(defn assert-medico [data]
  (base/assert Medico data))

(comment
  (mg/sample Medic))