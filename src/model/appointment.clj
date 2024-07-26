(ns model.appointment
  (:require [malli.generator :as mg]))

(def Appointment
  [:map {:encode/db (fn [x] (assoc x :xt/type :appointment))}
   [:medic-id :string]
   [:date :string]
   [:time :string]
   [:patient-id :string]])


(def ManageAppointment
  [:map
   [:patient-id :string]
   [:action :string]])

(comment
  (mg/sample Appointment))