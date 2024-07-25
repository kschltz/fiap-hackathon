(ns usecase.medic-search-test
  (:require [clojure.test :refer :all]
            [usecase.medic-search :as uc.medic-search]
            [xtdb.api :as xt] ))


(deftest medic-search-test
  (with-open [node (xt/start-node {})]
    (xt/await-tx node
                 (xt/submit-tx node [[:xtdb.api/put
                                      {:nome          "Serginho",
                                       :especialidade "oftalmologia",
                                       :senha         "20",
                                       :crm           "343230-01-SP"
                                       :xt/id         "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6",
                                       :xt/type       :medico}]]))

    (let [result (first (uc.medic-search/search-medic node "oftalmologia"))]
      (is (= "Serginho" (:nome result)))
      (is (= "oftalmologia" (:especialidade result))))))
