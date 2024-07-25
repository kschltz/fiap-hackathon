(ns http.routes-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [hato.client :as client]
            [core]
            [integrant.core :as ig]))

(defn sys-fixture [f]
  (let [s (core/-main "test")]
    (f)
    (ig/halt! s)))

(defn token [user-type]
  (if (= "medico" user-type)
    (-> (client/post "http://0.0.0.0:8081/login"
                     {:content-type      :json
                      :throw-exceptions? false
                      :body              (json/write-str
                                           {:crm   "654536-44-SP"
                                            :senha "pass"})})
        :body
        (json/read-str :key-fn keyword)
        :token)))

(use-fixtures :once sys-fixture)

(deftest test-search-medic
  (testing "Successful search"
    (let [response (-> (client/get
                         "http://localhost:8081/medic?especialidade=oftalmologia"
                         {:content-type      :json
                          :throw-exceptions? false
                          :headers           {"Authorization" (token "medico")}})
                       (update :body
                               #(json/read-str % :key-fn keyword)))]
      (is (= 200 (:status response)))
      (is (some? (:medics (:body response))))))

  (testing "Search with invalid parameter"
    (let [response (client/get "http://localhost:8081/medic?especialidade=invalid"
                               {:content-type      :json
                                :throw-exceptions? false
                                :headers           {"Authorization" (token "medico")}})]
      (is (= 200 (:status response)))
      (is (empty? (:medics (:body response)))))))

(deftest test-save-calendar
  (testing "Save calendar successfully"
    (let [response (client/post
                     "http://localhost:8081/medic/calendar"
                     {:content-type      :json
                      :throw-exceptions? false
                      :headers           {"Authorization" (token "medico")}
                      :body              (json/write-str {:year           2024 :month 9 :day 19
                                                          :availabilities [{:from "08:00" :to "12:00"}]})})]
      (is (= 200 (:status response)))))

  (testing "Save calendar with invalid data"
    (let [response (client/post "http://localhost:8081/medic/calendar"
                                {:content-type      :json
                                 :throw-exceptions? false
                                 :headers           {"Authorization" (token "medico")}
                                 :body              (json/write-str {:year 2024 :month 9})})]
      (is (= 500 (:status response))))))


(deftest test-login
  (testing "Successful login"
    (let [response (client/post "http://localhost:8081/login"
                                {:content-type      :json
                                 :throw-exceptions? false
                                 :body              (json/write-str
                                                      {:crm   "654536-44-SP",
                                                       :senha "pass"})})]
      (is (= 200 (:status response)))))

  (testing "Login with invalid credentials"
    (let [response (client/post "http://localhost:8081/login"
                                {:content-type      :json
                                 :throw-exceptions? false
                                 :body              (json/write-str {:crm "invalid" :senha "wrong"})})]
      (is (= 404 (:status response))))))

