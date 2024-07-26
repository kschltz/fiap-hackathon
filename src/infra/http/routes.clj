(ns infra.http.routes
  (:require [clojure.data.json :as json]
            [usecase.login :as uc.login]
            [infra.http.server :as server]
            [infra.http.routes.medic :as routes.medic]
            [infra.http.routes.appointment :as routes.appointment]
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

(defn secret [_x] {:status 200 :body "secret"})

(defmethod ig/init-key ::routes [_ {:keys []}]
  [[["/secret" ^:interceptors [server/authenticate-interceptor]
     {:get `secret}]
    ["/login"
     {:get  `echo
      :post `login}]
    routes.medic/routes
    routes.appointment/routes]])


(comment


  (hato.client/post
    "http://localhost:8080/login"
    {:content-type :json
     :body         (json/write-str {:crm   "654536-44-SP",
                                    :senha "pass"})})

  (hato.client/get
    "http://localhost:8080/secret"
    {:content-type :json
     :headers
     {"Authorization"
      "Bearer eyJhbGciOiJIUzI1NiJ9.IntcIm5vbWVcIjpcIkRlbnRpc3RvXCIsXCJlc3BlY2lhbGlkYWRlXCI6XCJvZG9udG9sb2dpYVwiLFwidHlwZVwiOlwibWVkaWNvXCIsXCJjcm1cIjpcIjY1NDUzNi00NC1TUFwiLFwic2VuaGFcIjpcImJjcnlwdCtzaGE1MTIkMTMyMDg0MzcxN2ZmMTc3OWY1ZDVjNTZiNjYxMjZmYmMkMTIkMTFiMjRiNmE3ZmEwY2Q2MTE5NzlmNDM3NGNmZGY4ZWY2Zjg0NzQwNzhiMTY0YWYyXCIsXCJpZFwiOlwiYzNlNDdjMjktMzFiNC00ZTQ5LWE0MDgtMTc5NzcyODIzZjdjXCIsXCJleHBpcmVzLWF0XCI6XCIyMDI0LTA3LTIzVDIxOjMxOjUwLjU5MzU5NjgzMlwifSI.KFxu_kcBo8Dmalwn-XXp64zoVg5b7YQppn6dOdj0o7M"}})

  )