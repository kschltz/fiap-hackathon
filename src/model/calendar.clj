(ns model.calendar
  (:require [clojure.test.check.generators :as gen]
            [malli.core :as m]
            [model.base :as base])
  (:import (java.time LocalTime)))

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
(def Availability
  [:and [:map
         [:from Time]
         [:to Time]]
   [:fn (fn [{:keys [from to]}]
          (.isAfter to from))]])

(def Calendar
  [:map {:encode/db (fn [{:keys [year month day medic-id] :as x}]
                      (-> x
                          (assoc :xt/type :calendar)
                          (assoc :xt/id (str year "-" month "-" day "#" medic-id))))}
   [:medic-id :uuid]
   [:year [:int {:min 2024 :max 3000}]]
   [:month [:int {:min 1 :max 12}]]
   [:day [:int {:min 1 :max 31}]]
   [:availabilities [:sequential Availability]]])

(defn assert-calendar [x]
  (base/assert Calendar x))

