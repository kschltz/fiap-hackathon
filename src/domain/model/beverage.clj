(ns domain.model.beverage
  (:require [domain.model.validation :as validation]))

(def types
  [:enum "alcoholic" "non-alcoholic"])

(def BeverageId :uuid)

(def beverage-domain ::beverage)

(def Beverage
  [:map
   [:xt/id BeverageId]
   [:xt/type [:= beverage-domain]]
   [:name :string]
   [:type types]
   [:price-cents pos-int?]])

(defn assert [data]
  (validation/assert Beverage data))
