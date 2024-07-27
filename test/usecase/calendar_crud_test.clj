(ns usecase.calendar-crud-test
  (:require [clojure.test :refer :all]
            [usecase.calendar-crud :as uc.calendar-crud]
            [xtdb.api :as xt])
  (:import (java.time LocalTime)))


(deftest create-calendar-test
  (with-open [node (xt/start-node {})]
    (let [sample-calendar {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                           :year           2024
                           :month          9
                           :day            19
                           :availabilities [{:from (LocalTime/of 8 0)
                                             :to   (LocalTime/of 12 0)}]}]
      (xt/await-tx node (uc.calendar-crud/create-calendar node sample-calendar))
      (let [result (xt/q (xt/db node) '{:find  [(pull e [*])]
                                        :where [[e :xt/type :calendar]]})]
        (is (= [{:availabilities [{:from (LocalTime/of 8 0)
                                   :to   (LocalTime/of 12 0)}]
                 :day            19
                 :medic-id       #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :month          9
                 :xt/id          "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :xt/type        :calendar
                 :year           2024}]
               (first result)))))))


(deftest update-calendar-test
  (with-open [node (xt/start-node {})]
    (let [initial-calendar {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                            :year           2024
                            :month          9
                            :day            19
                            :availabilities [{:from (LocalTime/of 8 0)
                                              :to   (LocalTime/of 12 0)}]}
          created-calendar (xt/await-tx node (uc.calendar-crud/create-calendar node initial-calendar))
          updated-calendar (-> (update initial-calendar :availabilities
                                       #(conj % {:from (LocalTime/of 14 0)
                                                 :to   (LocalTime/of 19 0)}))
                               (assoc :id (str (:xtdb.api/tx-id created-calendar))))]

      ;; Update calendar
      (xt/await-tx node (uc.calendar-crud/update-calendar node updated-calendar))

      ;; Verify update
      (let [result (xt/q (xt/db node) '{:find  [(pull e [*])]
                                        :where [[e :xt/type :calendar]]})]
        (is (= [{:availabilities [{:from (LocalTime/of 8 0)
                                   :to   (LocalTime/of 12 0)}
                                  {:from (LocalTime/of 14 0)
                                   :to   (LocalTime/of 19 0)}]
                 :day            19
                 :id             "0"
                 :medic-id       #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :month          9
                 :xt/id          "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :xt/type        :calendar
                 :year           2024}]
               (first result)))))))


(deftest book-appointment-test
  (with-open [node (xt/start-node {})]
    (let [initial-calendar {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                            :year           2024
                            :month          9
                            :day            19
                            :availabilities [{:from (LocalTime/of 8 0)
                                              :to   (LocalTime/of 12 0)
                                              :booked false}]}
          patient-id #uuid"12345678-1234-1234-1234-123456789012"]
      (xt/await-tx node (uc.calendar-crud/create-calendar node initial-calendar))
      (xt/await-tx node (uc.calendar-crud/book-appointment node #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6" "2024-9-19" (LocalTime/of 8 0) patient-id))
      (let [result (xt/q (xt/db node) '{:find  [(pull e [*])]
                                        :where [[e :xt/type :calendar]]})]
        (is (= [{:availabilities [{:booked     true
                                   :from       (LocalTime/of 8 0)
                                   :patient-id #uuid "12345678-1234-1234-1234-123456789012"
                                   :to         (LocalTime/of 12 0)}]
                 :day            19
                 :id             "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :medic-id       #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :month          9
                 :xt/id          "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :xt/type        :calendar
                 :year           2024}]
               (first result)))))))

(deftest unbook-appointment-test
  (with-open [node (xt/start-node {})]
    (let [initial-calendar {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                            :year           2024
                            :month          9
                            :day            19
                            :availabilities [{:from (LocalTime/of 8 0)
                                              :to   (LocalTime/of 12 0)
                                              :booked true
                                              :patient-id #uuid"12345678-1234-1234-1234-123456789012"}]}]
      (xt/await-tx node (uc.calendar-crud/create-calendar node initial-calendar))
      (xt/await-tx node (uc.calendar-crud/unbook-appointment node #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6" "2024-9-19" (LocalTime/of 8 0)))
      (let [result (xt/q (xt/db node) '{:find  [(pull e [*])]
                                        :where [[e :xt/type :calendar]]})]
        (is (= [{:availabilities [{:from (LocalTime/of 8 0)
                                   :to   (LocalTime/of 12 0)}]
                 :day            19
                 :id             "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :medic-id       #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :month          9
                 :xt/id          "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :xt/type        :calendar
                 :year           2024}]
               (first result)))))))