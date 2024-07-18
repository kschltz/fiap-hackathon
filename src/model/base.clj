(ns model.base
  (:require [malli.error :as me]
            [malli.core :as m]))

(defn assert [schema data]
  (if-let [result (m/explain schema data)]
    (throw (ex-info (str "Validation error: "
                         (me/humanize result))
                    {:data   data
                     :result result}))
    data))