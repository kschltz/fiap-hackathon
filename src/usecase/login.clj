(ns usecase.login
  (:require [clojure.data.json :as json]
            [xtdb.api :as xt]
            [model.base :as base]
            [infra.auth :as auth])
  (:import (java.time LocalDateTime)))


(defn login [xtdb-node auth {:keys [crm cpf senha]}]
  (let [db (xt/db xtdb-node)
        doc-clause (if crm
                     ['e :crm crm]
                     ['e :cpf (base/hash cpf)])
        user (some-> (ffirst (xt/q db
                                   {:find  '[(pull e [*])]
                                    :in    '[h-compare senha]
                                    :where [doc-clause
                                            '[e :senha s]
                                            '[(h-compare senha s)]]}
                                   base/hashed-compared
                                   senha))
                     (assoc :expires-at (str (.plusMinutes (LocalDateTime/now)
                                                           30))))]
    (auth/sign auth (json/write-str user))))


(comment
  (base/hashed-compared "pass" (base/hash "pass"))
  (->> (login (user/node) (user/auth)
              {:crm   "654536-44-SP",
               :senha "pass"})
       (auth/unsign (user/auth)))

  (xt/q (user/db)
        '{:find  [(pull e [*])]
          :where [[e :xt/id]]})
  )