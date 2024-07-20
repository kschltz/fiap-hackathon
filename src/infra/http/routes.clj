(ns infra.http.routes
  (:require [clojure.data.json :as json]
            [usecase.login :as uc.login]
            [integrant.core :as ig]))

(defn login [{:keys [app json-params]}]
  (try
    (let [{:keys [xtdb auth]} app
          token (uc.login/login xtdb auth json-params)]
      (if token
        {:status 200
         :body   {:token (str "Bearer " token)}}
        {:status 404}))
    (catch Exception e
      {:status  500
       :headers {"Content-Type" "application/json"}
       :body    (.getMessage e)})))

(defn echo [r]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (str r)})

(defmethod ig/init-key ::routes [_ {:keys []}]
  [[["/login"
     {:get  `echo
      :post `login}]]])


(comment

  (hato.client/post
    "http://hackathon-hmed-alb-1275940250.us-west-1.elb.amazonaws.com:8080/login"
    {:content-type :json
     :body         (json/write-str {:crm   "654536-44-SP",
                                    :senha "pass"})})
  )