(ns domain.adapter.http
  (:require [integrant.core :as ig]
            [domain.adapter.db.beverage :as db-beverage]
            [domain.model.storage-unit :refer [storage-unit-domain]]
            [domain.adapter.db.storage :as db-storage]
            [domain.usecase.storage :as usecase-storage]
            [io.pedestal.http.body-params :as body-params]
            [xtdb.api :as xt]))


(defn- list-beverages [{:keys [app] :as request}]
  (let [bev-type (get-in request [:query-params :type])
        clauses (when bev-type
                  [['e :type bev-type]])
        beverages (apply db-beverage/list (xt/db (:xtdb app)) clauses)]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    beverages}))

(defn- list-storage-units [{:keys [app]}]
  (let [units (db-storage/list (xt/db (:xtdb app)))]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    units}))

(defn- total-volume [{:keys [app] :as request}]
  (let [bev-type (get-in request [:query-params :type])
        volume (xt/q (xt/db (:xtdb app))
                     '{:find  [bev-type (sum volume)]
                       :in    [bev-type]
                       :where [[e :xt/type storage-unit-domain]
                               [e :usage usage]
                               [(bev-type usage) volume]]}
                     (keyword bev-type))]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    {:volume volume}}))


(defn- storage-capacity [{:keys [app] :as request}]
  (let [bev-type (keyword (get-in request [:query-params :type]))
        non-alcoholic? (= bev-type :non-alcoholic)
        liters (Integer/parseInt (get-in request [:query-params :liters]))
        units (->> (db-storage/list (xt/db (:xtdb app)))
                   (filter (fn [{:keys [capacity usage]}]
                             (and
                               (if non-alcoholic? (= (:alcoholic usage) 0) true)
                               (if (not non-alcoholic?) (= (:non-alcoholic usage) 0) true)
                               (-> (merge-with - capacity usage)
                                   (get bev-type)
                                   (>= liters))))))]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    {:availability units}}))

(defn- storage-availability [{:keys [app] :as request}]
  (let [bev-type (keyword (get-in request [:query-params :type]))
        liters (Integer/parseInt (get-in request [:query-params :liters] 0))
        units (->> (db-storage/list (xt/db (:xtdb app)))
                   (filter (fn [{:keys [usage]}]
                             (<= liters (get usage bev-type)))))]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    {:availability units}}))


(defn- movement-log [{:keys [app] :as request}]
  (let [{:keys [bev-type section storage-order time-order]
         :or   {storage-order :asc
                time-order    :desc}}
        (get request :query-params)
        clauses (cond-> []
                        section (conj ['su section])
                        bev-type (conj ['bev :type (keyword bev-type)]))
        log (db-storage/movement-log
              (xt/db (:xtdb app))
              :clauses clauses
              :order [['n (keyword storage-order)]
                      ['t (keyword time-order)]])]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    log}))

(defn- store-beverage [{:keys [app] :as request}]
  (let [{:keys [storage-unit-id
                beverage-id
                liters
                employee]} (get request :json-params)
        db (xt/db (:xtdb app))
        [storage-unit beverage] (xt/pull-many db '[*] [storage-unit-id
                                                       (parse-uuid beverage-id)])
        {:keys [storage-unit storage-movement] :as updated-state} (usecase-storage/store-beverage storage-unit beverage liters employee false)
        tx-res (xt/submit-tx (:xtdb app)
                             [[::xt/put storage-unit]
                              [::xt/put storage-movement]])]

    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (assoc updated-state :tx tx-res)}))


(defmethod ig/init-key ::routes [_ {:keys []}]
  [[["/beverage" {:get `list-beverages}
     ["/store" {:post `store-beverage} ^:interceptors [(body-params/body-params)]]]
    ["/storage-unit" {:get `list-storage-units}
     ["/capacity" {:get `storage-capacity}]
     ["/availability" {:get `storage-availability}]
     ["/log" {:get `movement-log}]]
    ["/analytics"
     ["/total-volume" {:get `total-volume}]]]])
