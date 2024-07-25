(ns usecase.login-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [usecase.login :as uc.login]
            [xtdb.api :as xt]
            [model.base :as base]
            [infra.auth :as auth]))

(deftest login-test
  (with-open [node (xt/start-node {})]
    (let [mock-auth-result "mock-jwt-token"]
      (with-redefs [auth/sign (fn [_ user-data]
                                (testing "should sign the user data with the jwt secret key"
                                  (is (= "1234" (:crm (json/read-str user-data :key-fn keyword)))))
                                mock-auth-result)]
        (xt/await-tx node (xt/submit-tx node [[::xt/put {:xt/id :user1 :crm "1234" :senha (base/hash "password")}]]))
        (let [result (uc.login/login node {} {:crm "1234" :senha "password"})]
          (is (= mock-auth-result result)))))))