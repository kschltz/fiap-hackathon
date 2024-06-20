(ns core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [aero.core :as aero]))

(defn -main [& [profile]]
  (let [profile (-> profile (or "dev") keyword)
        config (aero/read-config (io/resource "config.edn")
                                 {:profile profile})
        _ (ig/load-namespaces config)
        system (ig/init config)]))
