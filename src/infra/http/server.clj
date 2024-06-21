(ns infra.http.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.http.route :as route]
            [integrant.core :as ig]))

(defmethod ig/init-key ::server [_ {:keys [port routes join?]
                                    :or   {port   8080
                                           routes #{["/hello"
                                                     :get (fn [req] (tap> req)
                                                            {:status 200
                                                             :body   (str req)})
                                                     :route-name :hello]}}}]
  (let [routes (route/expand-routes routes)
        _ (tap> routes)
        server (http/create-server {::http/routes  routes
                                    ::http/type    :jetty
                                    ::http/join?   join?
                                    ::http/host    "0.0.0.0"
                                    ::http/port    port
                                    ::http/tracing (interceptor/interceptor
                                                     {:leave (fn [ctx] (tap> ctx) nil)})})]


    (http/start server)))


(defmethod ig/halt-key! ::server [_ server]
  (http/stop server))