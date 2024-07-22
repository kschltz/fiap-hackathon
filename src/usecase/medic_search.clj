(ns usecase.medic-search
  (:require [xtdb.api :as xt]))



(defn search-medic [xtdb-node especialidade]
  (->> (xt/q
         (xt/db xtdb-node)
         '{:find     [(pull e [*]) nome]
           :in       [especialidade]
           :order-by [[nome :desc]]
           :where    [[e :especialidade especialidade]
                      [e :xt/type :medico]
                      [e :nome nome]]}
         especialidade)
       (map first)))


(comment
  (search-medic (user/node) "oftalmologia")

  )