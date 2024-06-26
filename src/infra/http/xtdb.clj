(ns infra.http.xtdb
  (:require [xtdb.api :as xtdb]
            [integrant.core :as ig]))

(defmethod ig/init-key ::xtdb [_ cfg]
  (xtdb/start-node cfg))

(defmethod ig/halt-key! ::xtdb [_ node]
  (.close node))
