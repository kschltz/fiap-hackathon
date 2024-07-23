(ns infra.http.routes.medic
  (:require [infra.http.server :as server]
            [usecase.medic-search :as uc.medic-search]))

(defn search-medic [{:keys [app query-params]}]
  (try
    (let [{:keys [xtdb]} app
          especialidade (:especialidade query-params)
          medics (uc.medic-search/search-medic xtdb especialidade)]
      {:status 200
       :body   {:medics medics}})
    (catch Exception e
      {:status  500
       :headers {"Content-Type" "application/json"}
       :body    (.getMessage e)})))

(defn save-calendar [{:keys [app json-params]}]
  (try
    (let [{:keys [xtdb]} app]

      {:status 200})
    (catch Exception e
      {:status  500
       :headers {"Content-Type" "application/json"}
       :body    (.getMessage e)})))

(def routes
  ["/medic" ^:interceptors [server/authenticate-interceptor
                            (server/type-exclusive-interceptor "paciente")]

   {:get `search-medic}
   ["/calendar" {:post `save-calendar}]])


(comment

  (hato.client/get "http://localhost:8080/medic?especialidade=oftalmologia"
                  {:content-type :json})
  )
