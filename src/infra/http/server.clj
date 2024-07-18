(ns infra.http.server
  (:require [clojure.data.json :as json]
            [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.error :as error]))

(defn- hello [_req]
  (tap> [::hello _req])
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello, World!"})

(def service-error-handler
  (error/error-dispatch
    [ctx ex]
    [{:exception-type :clojure.lang.ExceptionInfo}]
    (if (= :schema (:error/type (meta (ex-data ex))))
      (assoc ctx :response {:status 400 :body {:message (ex-message ex)
                                               :error   (ex-data ex)}})

      (assoc ctx :response {:status 500 :body {:message (ex-message ex)
                                               :error   (str (ex-cause ex))}}))


    :else
    (assoc ctx :io.pedestal.interceptor.chain/error ex)))
(defmethod ig/init-key ::server [_ {:keys [port routes join? app-context]
                                    :or   {port   8080
                                           routes [[["/"
                                                     {:get `hello}]]]}}]
  (let [routes (into [] routes)
        server (-> {::http/routes          routes
                    ::http/initial-context {:app app-context}
                    ::http/type            :jetty
                    ::http/join?           join?
                    ::http/host            "0.0.0.0"
                    ::http/port            port}
                   (http/default-interceptors)
                   (update ::http/interceptors conj
                           (body-params)
                           (interceptor/interceptor
                             {:name  ::set-app-ctx
                              :enter (fn [{:keys [app] :as ctx}]
                                       (assoc-in ctx [:request :app] app))})
                           (interceptor/interceptor
                             {:name  ::json-body
                              :leave (fn [ctx]
                                       (update-in ctx [:response :body] json/write-str))})
                           (interceptor/interceptor
                             {:name  :tracing
                              :enter (fn [ctx] (tap> [::enter ctx]) ctx)
                              :leave (fn [ctx] (tap> [::leave ctx]) ctx)})
                           (interceptor/interceptor
                             {:name  ::error-handler
                              :error (fn [ctx]
                                       (tap> [::error ctx])

                                       ctx)})
                           service-error-handler)
                   (http/create-server))]

    (http/start server)))


(defmethod ig/halt-key! ::server [_ server]
  (http/stop server))