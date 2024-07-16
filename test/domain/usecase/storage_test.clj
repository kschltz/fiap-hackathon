(ns domain.usecase.storage-test
  (:require [clojure.test :refer :all]
            [domain.model.beverage :as beverage]
            [domain.model.storage-unit :as storage-unit]
            [domain.usecase.storage :refer :all]
            [malli.generator :as mg]))

(deftest store-beverage-happy-path
  (testing "Storing beverage increases usage"
    (let [storage (merge (mg/generate storage-unit/StorageUnit)
                         {:usage {:alcoholic     0
                                  :non-alcoholic 0}})
          beverage (merge (mg/generate beverage/Beverage)
                          {:type "non-alcoholic"})
          result (store-beverage storage
                                 beverage
                                 10
                                 "Kirk"
                                 false)]
      (is (= {:alcoholic     0
              :non-alcoholic 10}
             (:usage result))))))

(defn test-storage [a-usage n-usage]
  {:xt/id 1
   :xt/type :domain.model.storage-unit/storage-unit
   :name "Store stuff"
   :capacity {:alcoholic 500 :non-alcoholic 400}
   :usage {:alcoholic a-usage :non-alcoholic n-usage}})


(defn test-beverage [alcoholic?]
  {:xt/id #uuid"b95b74ff-a014-4e59-92ef-88b1d4a88ccc"
   :xt/type :domain.model.beverage/beverage
   :name "glurp"
   :type (if alcoholic? "alcoholic" "non-alcoholic")
   :price-cents 200})

(deftest store-beverage-exceeds-capacity
  (testing "Storing beverage that exceeds capacity throws exception"
     (try
       (store-beverage (test-storage 0 400)
                       (test-beverage false)
                       200 "furbs" false)
           (catch Exception e
             (is (= "{:usage {:non-alcoholic [\"should be at most 400\"]}}"
                    (ex-message e)))))))

(deftest store-beverage-alcohol-in-last-day
  (testing "Storing non-alcoholic beverage after storing alcoholic beverage throws exception"
    (try
      (store-beverage (test-storage 0 10)
                      (test-beverage false)
                      200 "furbs" true)
      (catch Exception e
        (is (= "Can't store non-alcoholic beverage due to recent alcoholic volume stored"
               (ex-message e)))))))

