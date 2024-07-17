(ns infra.auth
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hato.client :as hc]
            [integrant.core :as ig]))

(defmethod ig/init-key ::auth [_ cfg]
  (log/info "Authenticating user" cfg)
  cfg)


(defn get-jwt [{:keys [domain client_id client_secret audience]}]
  (-> (hc/post (str "https://" domain "/oauth/token")
               {:form-params       {:client_id     client_id
                                    :client_secret client_secret
                                    :audience      audience
                                    :grant_type    "client_credentials"}
                :throw-exceptions? false})
      :body
      (json/read-str :key-fn keyword)))
