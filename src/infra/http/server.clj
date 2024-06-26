(ns infra.http.server
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as interceptor]))

(defn- hello [_req]
  (tap> [::hello _req])
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello, World!"})

(defmethod ig/init-key ::server [_ {:keys [port routes join? app-context]
                                    :or   {port   8080
                                           routes [[["/"
                                                     {:get `hello}]]]}}]
  (let [routes (into [] routes)
        server (http/create-server {::http/routes          routes
                                    ::http/initial-context {:app app-context}
                                    ::http/type            :jetty
                                    ::http/join?           join?
                                    ::http/host            "0.0.0.0"
                                    ::http/port            port
                                    ::http/tracing         (interceptor/interceptor
                                                             {:name  :tracing
                                                              :leave (fn [ctx] (tap> ctx) ctx)})})]
    (http/start server)))


(defmethod ig/halt-key! ::server [_ server]
  (http/stop server))