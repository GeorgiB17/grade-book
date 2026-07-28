(ns gradebook.core
  "Wires everything together. This is the ONLY namespace that prints anything."
  (:require [clojure.string :as str]
            [gradebook.data :as data]
            [gradebook.grades :as grades]
            [gradebook.report :as report]
            [gradebook.debug :refer [spy]]))

(defn full-report
  "The whole report as one string. Still a pure function — it prints nothing,
  so you can test it, or write it to a file, or ignore it."
  [students]
  (str/join
   "\n"
   [(report/heading "Class list (best first)")
    report/table-header
    (report/table (grades/ranked students))

    (report/heading "Summary")
    (report/summary-lines (grades/summary students))

    (report/heading "Grade distribution")
    (report/distribution-chart (grades/grade-distribution students))

    (report/heading "Group averages")
    (report/group-chart (grades/group-averages students))]))

;; ---------------------------------------------------------------------------
;; Everything below here has side effects. Everything above does not.
;; ---------------------------------------------------------------------------

(defn- show-immutability
  "Proof, on screen, that adding a score changes nothing."
  []
  (println (report/heading "Nothing is ever modified"))
  (let [ada     (first data/students)
        updated (grades/add-score ada 100)]
    (println "  original Ada :scores  " (:scores ada))
    (println "  after add-score       " (:scores updated))
    (println "  original again        " (:scores ada) " <- still the same")))

(defn- show-spy
  "The `spy` macro, used the way you would actually use it."
  []
  (println (report/heading "The spy macro"))
  (let [best (spy (grades/top data/students 1))]
    (println "  and the value came back out:" (:name (first best)))))

(defn -main
  [& _]
  (println (full-report data/students))
  (show-immutability)
  (show-spy)
  (println))
