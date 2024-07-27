(ns http.appointment-test
  (:require [clojure.test :refer :all]
            [hato.client :as client]
            [clojure.data.json :as json]
            [core]
            [integrant.core :as ig]))

(defn sys-fixture [f]
  (let [s (core/-main "test")]
    (f)
    (ig/halt! s)))

(use-fixtures :once sys-fixture)

(deftest create-appointment-test
  (testing "Create appointment successfully"
    (let [response (client/post
                     "http://localhost:8081/patient/appointment"
                     {:content-type      :json
                      :throw-exceptions? true
                      :headers           {}
                      :body              (json/write-str
                                           {:medic-id "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                                            :date     "2024-09-19"
                                            :time     "08:00"})})]
      (is (= 200 (:status response)))
      (is (= {:message "Appointment requested successfully"}
             (json/read-str (:body response) :key-fn keyword))))))
