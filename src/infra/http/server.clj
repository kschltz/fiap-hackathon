(ns infra.http.server
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [infra.auth :as auth]
            [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.error :as error])
  (:import (java.time LocalDateTime)))

(defn- hello [_req]
  (tap> [::hello _req])
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello, World!"})

(def authenticate-interceptor
  (interceptor/interceptor
    {:name  ::auth-interceptor
     :enter (fn [{{:keys [auth]} :app :as ctx}]
              (tap> (get-in ctx [:request :headers "authorization"]))
              (let [{:strs [expires-at type] :as token} (->> (str/replace (get-in ctx [:request :headers "authorization"]) #"^Bearer " "")
                                                             (auth/unsign auth)
                                                             json/read-str)]

                (if (and type (.isBefore (LocalDateTime/now)
                                         (LocalDateTime/parse expires-at)))
                  (assoc-in ctx [:request :user] token)
                  (assoc ctx :response {:status 401 :body {:message "Unauthorized"}}))))}))

(defn type-exclusive-interceptor [& types]
  (interceptor/interceptor
    {:name  ::type-exclusive-interceptor
     :enter (fn [{{{:strs [type]} :user} :request :as ctx}]
              (tap> [::36 types type (contains? (set types) type)])
              (if (contains? (set types) type)
                ctx
                (assoc ctx :response {:status 403 :body {:message "Forbidden"}})))}))

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