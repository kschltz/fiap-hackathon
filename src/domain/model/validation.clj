(ns domain.model.validation
  (:require [malli.core :as m]
            [malli.error :as me]))


(defn assert [schema data]
  (if-let [error (m/explain schema data)]
    (throw (ex-info (str (me/humanize error))
                    (with-meta data {:error/type :schema})))
    data))
