(ns model.paciente
  (:require [model.base :as base]
            [value-object.cpf :as cpf]))

(def Paciente
  [:map {:encode/db (fn [x] (assoc x :xt/type :paciente))}
   [:nome :string]
   [:senha {:encode/db base/hash} :string]
   [:cpf {:encode/db model.base/hash} cpf/CPF]])

(defn assert-paciente [data]
  (base/assert Paciente data))


