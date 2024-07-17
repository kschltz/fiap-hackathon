(ns value-object.crm
  (:require [clojure.spec.gen.alpha :as gen]
            [malli.core :as m]))

(def crm-regex #"^\d{6}-\d{2}\/[A-Z]{2}$")

(defn valid? [crm]
  (re-matches crm-regex crm))

(defn gen-crm []
  (gen/fmap (fn [s] (str (apply str (take 6 s)) "-"
                         (apply str (drop 6 s))
                         "-SP"))
            (gen/vector (gen/fmap abs (gen/int {:size 9})) 8)))

(def CRM
  (m/-simple-schema
    {:type            ::CRM
     :pred            valid?
     :type-properties {:error-message "CRM inválido"
                       :gen/gen       (gen-crm)}}))

(comment
  (gen/sample (gen-crm)))