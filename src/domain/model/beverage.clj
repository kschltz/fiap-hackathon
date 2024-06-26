(ns domain.model.beverage)

(def types
  [:enum "alcoholic" "non-alcoholic"])

(def BeverageId :uuid)

(def Beverage
  [:map
   [:id BeverageId]
   [:name :string]
   [:type types]
   [:price-cents pos-int?]])
