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
        :token)
    (-> (client/post "http://0.0.0.0:8081/login"
                     {:content-type      :json
                      :throw-exceptions? false
                      :body              (json/write-str
                                           {:cpf   "61999193784"
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

(deftest create-appointment-test
  (testing "Create appointment successfully"
    (let [response (client/post
                     "http://localhost:8081/patient/appointment"
                     {:content-type      :json
                      :throw-exceptions? true
                      :headers           {"Authorization" (token "paciente")}
                      :body              (json/write-str
                                           {:medic-id "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                                            :date     "2024-09-19"
                                            :time     "08:00"})})]
      (is (= 200 (:status response)))
      (is (= {:message "Appointment requested successfully"}
             (json/read-str (:body response) :key-fn keyword))))))

(deftest delete-appointment-test
  (testing "Delete appointment successfully"
    (let [_ (client/post
                              "http://localhost:8081/medic/calendar"
                              {:content-type      :json
                               :throw-exceptions? false
                               :headers           {"Authorization" (token "medico")}
                               :body              (json/write-str {:year           2024 :month 9 :day 19
                                                                   :availabilities [{:from "08:00" :to "12:00"}]})})
          create-response (client/post
                            "http://localhost:8081/patient/appointment"
                            {:content-type      :json
                             :throw-exceptions? true
                             :headers           {"Authorization" (token "paciente")}
                             :body              (json/write-str
                                                  {:medic-id "c3e47c29-31b4-4e49-a408-179772823f7c"
                                                   :date     "2024-09-19"
                                                   :time     "08:00"})})]
      (is (= 200 (:status create-response)))
      (is (= {:message "Appointment requested successfully"}
             (json/read-str (:body create-response) :key-fn keyword)))

      (let [delete-response (client/put
                              "http://localhost:8081/patient/appointment"
                              {:content-type      :json
                               :throw-exceptions? true
                               :headers           {"Authorization" (token "paciente")}
                               :body              (json/write-str
                                                    {:medic-id "c3e47c29-31b4-4e49-a408-179772823f7c"
                                                     :date     "2024-09-19"
                                                     :time     "08:00"})})]
        (is (= 200 (:status delete-response)))
        (is (= {:message "Appointment deleted successfully"}
               (json/read-str (:body delete-response) :key-fn keyword)))))))

(deftest manage-appointment-test
  (testing "Manage appointment successfully"
    (client/post
      "http://localhost:8081/patient/appointment"
      {:content-type      :json
       :throw-exceptions? true
       :headers           {"Authorization" (token "paciente")}
       :body              (json/write-str
                            {:medic-id "c3e47c29-31b4-4e49-a408-179772823f7c"
                             :date     "2024-09-19"
                             :time     "08:00"})})

    (let [response (client/put
                     "http://localhost:8081/medic/appointment"
                     {:content-type      :json
                      :throw-exceptions? true
                      :headers           {"Authorization" (token "medico")}
                      :body              (json/write-str
                                           {:appointment-id "2024-9-19#c3e47c29-31b4-4e49-a408-179772823f7c#08:00#6cc4fbf9-a78c-4d97-b89d-bb3cf585a2b3"
                                            :action         "reject"})})]
      (is (= 200 (:status response)))
      (is (= {:message "Appointment managed successfully"}
             (json/read-str (:body response) :key-fn keyword))))))