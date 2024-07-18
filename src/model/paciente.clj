(ns model.paciente
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [value-object.cpf :as cpf]
            [model.base :as base]))

(def Paciente
  [:map
   [:nome :string]
   [:cpf {:encode/db model.base/hash} cpf/CPF]])

(defn assert-paciente [data]
  (base/assert Paciente data))


