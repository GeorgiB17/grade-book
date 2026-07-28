(ns gradebook.data
  "The data. That is all this namespace is.

  A student is a MAP. The whole class is a VECTOR of maps.
  ")

(def students
  [{:name "Ada"    :group :morning :scores [92 88 95]}
   {:name "Bruno"  :group :morning :scores [55 61 48]}
   {:name "Chen"   :group :evening :scores [78 82 80]}
   {:name "Dara"   :group :evening :scores [65 70 74]}
   {:name "Eli"    :group :morning :scores [88 91 84]}
   {:name "Fatima" :group :evening :scores [45 52 58]}
   {:name "Goran"  :group :morning :scores [71 68 75]}
   {:name "Georgi"   :group :evening :scores [96 94 99]}])
