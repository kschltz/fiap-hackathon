(ns model.paciente
  (:require [malli.generator :as mg]
            [value-object.cpf :as cpf]
            [model.base :as base]))

(def Paciente
  [:map
   [:nome :string]
   [:cpf cpf/CPF]])

(defn assert-paciente [data]
  (base/assert Paciente data))
