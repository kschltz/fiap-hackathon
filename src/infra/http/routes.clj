(ns infra.http.routes
  (:require [clojure.data.json :as json]
            [integrant.core :as ig]))


(defn login [{:keys [app json-params]}]
  (let [node (:xtdb app)]
    (tap> json-params)
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (str json-params)}))

(defn echo [r]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (str r)})

(defmethod ig/init-key ::routes [_ {:keys []}]
  [[["/login"
     {:get  `echo
      :post `login}]]])


(comment

  (hato.client/post "http://localhost:8080/login"
                    {:content-type :json
                     :body         (json/write-str {:email "email" :password "password"})})
  )