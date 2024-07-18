(ns user
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [aero.core :as aero]
            [xtdb.api :as xt]
            [hato.client :as hc]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]))

(integrant.repl/set-prep! #(ig/prep (doto (aero/read-config (io/resource "config.edn")
                                                            {:profile :dev})
                                      ig/load-namespaces)))

(defn node []
  (:infra.xtdb/xtdb integrant.repl.state/system))

(defn db []
  (xt/db (:infra.xtdb/xtdb integrant.repl.state/system)))

(defn auth []
  (:infra.auth/auth integrant.repl.state/system))

(defn store-bev [& {:keys [alcoholic? storage-unit-id]
                    :or   {storage-unit-id 1}}]

  (let [[b1 b2] (->
                  "http://localhost:8080/beverage"
                  (cond->
                    (true? alcoholic?) (str "?type=alcoholic")
                    (false? alcoholic?) (str "?type=non-alcoholic")
                    :else identity)
                  (hc/get)
                  :body
                  (json/read-str :key-fn keyword))]
    (tap> [b1 b2])
    (-> (hc/post "http://localhost:8080/beverage/store"
                 {:body              (json/write-str
                                       {:storage-unit-id storage-unit-id
                                        :beverage-id     (:id b1)
                                        :liters          25
                                        :employee        "KAUE"})
                  :content-type      :json
                  :throw-exceptions? false})
        (update :body json/read-str)))
  )