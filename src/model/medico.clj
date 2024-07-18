(ns model.medico
  (:require [malli.generator :as mg]
            [value-object.crm :as crm]
            [model.base :as base]))

(def Medico
  [:map
   [:nome :string]
   [:especialidade :string]
   [:crm crm/CRM]])

(defn assert-medico [data]
  (base/assert Medico data))

(comment
  (mg/sample Medic))