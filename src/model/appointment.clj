(ns model.appointment
  (:require [malli.generator :as mg]
            [malli.core :as m]
            [clojure.test.check.generators :as gen])
  (:import (java.time LocalTime LocalDate Year)))

(defn valid-time? [x]
  (instance? LocalTime x))

(def Time
  (m/-simple-schema
    {:type            ::Time
     :pred            valid-time?
     :type-properties {:decode/custom-json (fn [x] (LocalTime/parse x))
                       :error-message "horário inválido"
                       :gen/gen       (gen/fmap
                                        (fn [n] (LocalTime/of (mod n 12)
                                                              (mod n 60)))
                                        gen/small-integer)}}))


(defn valid-date? [x]
  (or (instance? LocalDate x)
      (re-matches  #"\d{4}-\d{2}-\d{2}$" x)))

(def Date
  (m/-simple-schema
    {:type            ::Date
     :pred            valid-date?
     :type-properties {:decode/custom-json (fn [x] (LocalDate/parse x))
                       :error-message "data inválida"
                       :gen/gen       (gen/fmap
                                        (fn [_] (LocalDate/of (inc (rand-int (Integer/parseInt (str (Year/now)))))
                                                              (inc (rand-int 12))
                                                              (inc (rand-int 28))))
                                        gen/small-integer)}}))

(def Appointment
  [:map {:encode/db (fn [{:keys [date time patient-id medic-id] :as x}]
                      (-> x
                          (assoc :xt/type :appointment)
                          (assoc :xt/id (str date "#" medic-id "#" time "#" patient-id))))}
   [:medic-id :string]
   [:date Date]
   [:time Time]
   [:patient-id :string]])


(def ManageAppointment
  [:map
   [:patient-id :string]
   [:action :string]])

(comment
  (mg/sample Appointment 3))