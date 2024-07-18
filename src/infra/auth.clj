(ns infra.auth
  (:require [buddy.core.codecs :as codecs]
            [buddy.sign.jws :as jws]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hato.client :as hc]
            [integrant.core :as ig]))

(defn get-public-key [{:keys [domain client_id client_secret audience]}]
  ;; Fetch the public key from the JWKS endpoint
  (let [response (-> (hc/get (str "https://" domain "/.well-known/jwks.json"))
                     :body
                     (json/read-str :key-fn keyword))]
    ;; Extract the public key from the response
    (-> response
        :keys
        first
        :x5c
        first
        codecs/b64->str)))

(defmethod ig/init-key ::auth [_ cfg]
  (assoc cfg :public-key (get-public-key cfg)))



(defn sign [auth data]
  (let [public-key (:public-key auth)]
    (jws/sign (json/write-str data) public-key)))

(defn unsign [auth data]
  (let [public-key (:public-key auth)]
    (json/read-str
      (String. (jws/unsign data public-key))
      :key-fn keyword )))


(comment
  (->> (sign (user/auth) {:sub "123" :name "John Doe"})
       (unsign (user/auth)))

  )