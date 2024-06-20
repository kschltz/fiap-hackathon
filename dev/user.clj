(ns user
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [aero.core :as aero]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]))

(integrant.repl/set-prep! #(ig/prep (doto (aero/read-config (io/resource "config.edn")
                                                            {:profile :dev})
                                      ig/load-namespaces)))