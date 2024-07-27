(ns usecase.appointment-test
  (:require [clojure.test :refer :all]
            [usecase.appointment :as uc.appointment]
            [usecase.calendar-crud :as uc.calendar-crud]
            [xtdb.api :as xt])
  (:import (java.time LocalTime)))

(deftest create-appointment-test
  (with-open [node (xt/start-node {})]
    (let [sample-appointment {:medic-id   #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                              :date       "2024-09-19"
                              :time       (LocalTime/of 8 0)
                              :patient-id #uuid"12345678-1234-1234-1234-123456789012"}]

      (xt/await-tx node (uc.appointment/create-appointment node sample-appointment))

      (let [result (xt/q (xt/db node) '{:find  [(pull e [*])]
                                        :where [[e :xt/type :appointment]]})]
        (is (= [{:date       "2024-9-19"
                 :medic-id   #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :patient-id #uuid "12345678-1234-1234-1234-123456789012"
                 :status     :pending
                 :time       (LocalTime/of 8 0)
                 :xt/id      "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6#08:00#12345678-1234-1234-1234-123456789012"
                 :xt/type    :appointment}]
               (first result)))))))


(deftest cancel-appointment-test
  (with-open [node (xt/start-node {})]
    (let [sample-appointment {:medic-id   #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                              :date       "2024-09-19"
                              :time       (LocalTime/of 8 0)
                              :patient-id #uuid"12345678-1234-1234-1234-123456789012"}
          sample-calendar {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                           :year           2024
                           :month          9
                           :day            19
                           :availabilities [{:from (LocalTime/of 8 0)
                                             :to   (LocalTime/of 9 0)}
                                            {:from (LocalTime/of 9 0)
                                             :to   (LocalTime/of 10 0)}]}]

      (xt/await-tx node (uc.calendar-crud/create-calendar node sample-calendar))
      (xt/await-tx node (uc.appointment/create-appointment node sample-appointment))

      (xt/await-tx node (uc.appointment/cancel-appointment node sample-appointment))

      (let [result (xt/q (xt/db node) '{:find  [(pull e [*])]
                                        :where [[e :xt/type :appointment]]})]
        (is (= [{:date       "2024-9-19"
                 :medic-id   #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                 :patient-id #uuid "12345678-1234-1234-1234-123456789012"
                 :status     :canceled
                 :time       (LocalTime/of 8 0)
                 :xt/id      "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6#08:00#12345678-1234-1234-1234-123456789012"
                 :xt/type    :appointment}]
               (first result)))))))

(deftest accept-appointment-test
  (with-open [node (xt/start-node {})]
    (let [sample-calendar {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                           :year           2024
                           :month          9
                           :day            19
                           :availabilities [{:from (LocalTime/of 8 0)
                                             :to   (LocalTime/of 9 0)}
                                            {:from (LocalTime/of 9 0)
                                             :to   (LocalTime/of 10 0)}]}
          sample-appointment {:medic-id   #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                              :date       "2024-09-19"
                              :time       (LocalTime/of 8 0)
                              :patient-id #uuid"12345678-1234-1234-1234-123456789012"
                              :status     :pending}
          appointment-id "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6#08:00#12345678-1234-1234-1234-123456789012"]

      (xt/await-tx node (uc.calendar-crud/create-calendar node sample-calendar))
      (xt/await-tx node (uc.appointment/create-appointment node sample-appointment))

      (xt/await-tx node (uc.appointment/accept-appointment node appointment-id))

      (let [calendar (first (xt/q (xt/db node) '{:find  [(pull e [*])]
                                                 :where [[e :xt/type :calendar]]}))
            result (first (xt/q (xt/db node) '{:find  [(pull e [*])]
                                               :where [[e :xt/type :appointment]]}))]
        (testing "Should return the appointment with status :accepted"
          (is (= {:date       "2024-9-19"
                  :medic-id   #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                  :patient-id #uuid "12345678-1234-1234-1234-123456789012"
                  :status     :accepted
                  :time       (LocalTime/of 8 0)
                  :xt/id      "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6#08:00#12345678-1234-1234-1234-123456789012"
                  :xt/type    :appointment}
                 (first result))))
        (testing "Should book available time in medic calendar"
          (is (= [{:availabilities [{:booked     true
                                     :from       (LocalTime/of 8 0)
                                     :patient-id #uuid "12345678-1234-1234-1234-123456789012"
                                     :to         (LocalTime/of 9 0)}
                                    {:from (LocalTime/of 9 0)
                                     :to   (LocalTime/of 10 0)}]
                   :day            19
                   :id             "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                   :medic-id       #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                   :month          9
                   :xt/id          "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                   :xt/type        :calendar
                   :year           2024}]
                 calendar)))))))

(deftest reject-appointment-test
  (with-open [node (xt/start-node {})]
    (let [sample-calendar {:medic-id       #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                           :year           2024
                           :month          9
                           :day            19
                           :availabilities [{:from (LocalTime/of 8 0)
                                             :to   (LocalTime/of 9 0)}
                                            {:from (LocalTime/of 9 0)
                                             :to   (LocalTime/of 10 0)}]}
          sample-appointment {:medic-id   #uuid"01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                              :date       "2024-09-19"
                              :time       (LocalTime/of 8 0)
                              :patient-id #uuid"12345678-1234-1234-1234-123456789012"
                              :status     :pending}
          appointment-id "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6#08:00#12345678-1234-1234-1234-123456789012"]

      (xt/await-tx node (uc.calendar-crud/create-calendar node sample-calendar))
      (xt/await-tx node (uc.appointment/create-appointment node sample-appointment))

      (xt/await-tx node (uc.appointment/reject-appointment node appointment-id))

      (testing "Should return appointment with status :rejected"
        (let [result (first (xt/q (xt/db node) '{:find  [(pull e [*])]
                                                 :where [[e :xt/type :appointment]]}))
              calendar (first (xt/q (xt/db node) '{:find  [(pull e [*])]
                                                   :where [[e :xt/type :calendar]]}))]
          (is (= {:date       "2024-9-19"
                  :medic-id   #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                  :patient-id #uuid "12345678-1234-1234-1234-123456789012"
                  :status     :rejected
                  :time       (LocalTime/of 8 0)
                  :xt/id      "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6#08:00#12345678-1234-1234-1234-123456789012"
                  :xt/type    :appointment}
                 (first result)))

          (testing "The calendar dont change"
            (is (= [{:availabilities [{:from (LocalTime/of 8 0)
                                       :to   (LocalTime/of 9 0)}
                                      {:from (LocalTime/of 9 0)
                                       :to   (LocalTime/of 10 0)}]
                     :day            19
                     :medic-id       #uuid "01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                     :month          9
                     :xt/id          "2024-9-19#01582fa7-cd5a-4f0a-be8c-9b776a6ca3d6"
                     :xt/type        :calendar
                     :year           2024}]
                   calendar))))))))