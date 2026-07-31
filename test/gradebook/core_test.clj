(ns gradebook.core-test
  "Run with:  clj -M -m gradebook.core-test"
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [gradebook.data :as data]
            [gradebook.grades :as grades]
            [gradebook.report :as report]
            [gradebook.core :as core]
            [gradebook.debug :refer [spy]]))

;; Small, hand-checkable test data — easier to reason about than the real class.
(def alice {:name "Alice" :group :a :scores [90 80]})   ; average 85.0 -> B
(def bob   {:name "Bob"   :group :b :scores [50 60]})   ; average 55.0 -> F
(def cara  {:name "Cara"  :group :a :scores [100 90]})  ; average 95.0 -> A
(def klass [alice bob cara])

(defn- close?
  "Doubles should never be compared with `=`, because 0.1 + 0.2 is not exactly
  0.3 in binary. Compare with a tolerance instead."
  [a b]
  (< (Math/abs (- a b)) 0.001))

;; ---------------------------------------------------------------------------

(deftest adding-up
  (is (= 6 (grades/sum [1 2 3])))
  (is (= 0 (grades/sum [])))
  (testing "all three versions agree"
    (let [numbers [4 8 15 16 23 42]]
      (is (= 108
             (grades/sum           numbers)
             (grades/sum-recursive numbers)
             (grades/sum-loop      numbers)))))
  (testing "loop/recur survives a list that would overflow the stack"
    (is (= 4999950000 (grades/sum-loop (range 100000))))))

(deftest averages
  (is (= 85.0 (grades/average [90 80])))
  (is (= 0.0  (grades/average [])))
  (is (= 85.0 (grades/student-average alice)))
  (is (close? 78.333 (grades/class-average klass))))

(deftest letter-grades
  (is (= "A" (grades/letter-grade 90)))
  (is (= "A" (grades/letter-grade 100)))
  (is (= "B" (grades/letter-grade 89.9)))
  (is (= "B" (grades/letter-grade 80)))
  (is (= "C" (grades/letter-grade 70)))
  (is (= "D" (grades/letter-grade 60)))
  (is (= "F" (grades/letter-grade 59.9)))
  (is (= "F" (grades/letter-grade 0)))
  (is (= "B" (grades/grade-of alice))))

(deftest passing-and-failing
  (is (true?  (grades/passing? alice)))
  (is (false? (grades/passing? bob)))
  (is (= ["Alice" "Cara"] (map :name (grades/passing-students klass))))
  (is (= ["Bob"]          (map :name (grades/failing-students klass)))))

(deftest sorting
  (is (= ["Cara" "Alice" "Bob"] (map :name (grades/ranked klass))))
  (is (= ["Cara" "Alice"]       (map :name (grades/top klass 2))))
  (testing "students with the same average are ordered by name, every run"
    (let [x {:name "Zoe" :group :a :scores [70]}
          y {:name "Amy" :group :a :scores [70]}]
      (is (= ["Amy" "Zoe"] (map :name (grades/ranked [x y])))))))

(deftest grouping-and-counting
  (is (= {"A" 1 "B" 1 "F" 1} (grades/grade-distribution klass)))
  (is (= {:a [alice cara] :b [bob]} (grades/by-group klass)))
  (is (= [[:a 90.0] [:b 55.0]] (grades/group-averages klass))))

;; ---------------------------------------------------------------------------

(deftest nothing-is-ever-modified
  (testing "add-score leaves the original student alone"
    (let [updated (grades/add-score alice 100)]
      (is (= [90 80]     (:scores alice)))
      (is (= [90 80 100] (:scores updated)))))

  (testing "add-score-to leaves the original class alone"
    (let [updated (grades/add-score-to klass "Bob" 100)]
      (is (= [50 60]     (:scores bob)))
      (is (= [50 60 100] (:scores (second updated))))
      (testing "and students who did not change are the very same object"
        (is (identical? alice (first updated))))))

  (testing "the same is true of any collection"
    (let [v [1 2 3]]
      (is (= [1 2 3 4] (conj v 4)))
      (is (= [1 2 3]   v)))))

(deftest summarising
  (let [s (grades/summary klass)]
    (is (= 3 (:count s)))
    (is (= 2 (:passing s)))
    (is (= 1 (:failing s)))
    (is (= "Cara" (:best s)))
    (is (close? 78.333 (:average s)))))

;; ---------------------------------------------------------------------------

(deftest reporting
  (is (= "███" (report/bar 3)))
  (is (= ""    (report/bar 0)))
  (is (= "\nHi\n--" (report/heading "Hi")))
  (testing "a student line mentions the name and the grade"
    (let [line (report/student-line alice)]
      (is (re-find #"Alice" line))
      (is (re-find #"B" line))))
  (testing "a failing student is flagged"
    (is (re-find #"failing" (report/student-line bob))))
  (testing "grades nobody earned still show, as zero"
    (is (re-find #"C   0" (report/distribution-chart {"A" 1})))))

(deftest the-whole-report
  (let [output (core/full-report klass)]
    (is (string? output))
    (is (re-find #"Alice" output))
    (is (re-find #"Summary" output))
    (testing "full-report is pure, so running it twice gives the same string"
      (is (= output (core/full-report klass))))))

;; ---------------------------------------------------------------------------

(deftest spy-macro
  (let [printed (with-out-str (print (spy (+ 1 2))))]
    (testing "it prints the source code of the expression"
      (is (re-find #"\+ 1 2" printed)))
    (testing "and hands the value back, so it can sit inside other code"
      (is (re-find #"3" printed))))
  (testing "it expands into a let, with core names qualified for us"
    ;; The symbol is written out in full here ON PURPOSE.
    ;;
    ;; `macroexpand-1` looks symbols up in whatever namespace is current when it
    ;; RUNS (the var `*ns*`). In a REPL sitting in this namespace, plain `spy`
    ;; resolves fine. But `clj -M -m` calls `-main` with `*ns*` bound to `user`,
    ;; where the name `spy` means nothing — so macroexpand-1 would decide it is
    ;; not a macro and hand the form straight back unexpanded, and this test
    ;; would fail depending only on HOW you ran it.
    ;;
    ;; Naming the var in full removes the guesswork: `gradebook.debug/spy`
    ;; resolves the same way from anywhere.
    (is (= 'clojure.core/let
           (first (macroexpand-1 '(gradebook.debug/spy (+ 1 2))))))))

;; ---------------------------------------------------------------------------

(deftest the-real-class
  (is (= 8 (count data/students)))
  (is (= "Georgi" (:name (first (grades/ranked data/students)))))
  (is (close? 75.375 (grades/class-average data/students)))
  (is (= 6 (count (grades/passing-students data/students)))))

(defn -main [& _]
  (let [{:keys [fail error]} (run-tests 'gradebook.core-test)]
    (System/exit (if (zero? (+ fail error)) 0 1))))
