(ns gradebook.report
  "Turning results into text. Every function here RETURNS a string and prints
  nothing. That means each one can be tested with `=`, and it keeps all the
  printing in one place (core/-main)."
  (:require [clojure.string :as str]
            [gradebook.grades :as grades]))

(defn bar
  [n]
  (apply str (repeat n "█")))

(defn heading
  [title]
  (str "\n" title "\n" (apply str (repeat (count title) "-"))))

(def table-header
  "  NAME     GROUP      AVG  GRADE")

(defn student-line
  [student]
  (format "  %-8s %-8s %6.2f    %s%s"
          (:name student)
          (name (:group student))              ; :morning -> "morning"
          (grades/student-average student)
          (grades/grade-of student)
          (if (grades/passing? student) "" "   <- failing")))

(defn table
  [students]
  (->> students
       (map student-line)
       (str/join "\n")))

(def ^:private grade-order ["A" "B" "C" "D" "F"])

(defn distribution-chart
  "`(get dist grade 0)` uses the third argument as a default, so a grade that
  nobody earned shows as 0 instead of nil."
  [dist]
  (->> grade-order
       (map (fn [grade]
              (let [n (get dist grade 0)]
                (format "  %s  %2d  %s" grade n (bar n)))))
       (str/join "\n")))

(defn group-chart
  [pairs]
  (->> pairs
       (map (fn [[group avg]] (format "  %-8s %6.2f" (name group) avg)))
       (str/join "\n")))

(defn summary-lines
  "Associative destructuring in the parameter list: the map's keys arrive as
  ready-made local names.

  Note that the local `count` shadows `clojure.core/count` inside this function.
  That is legal and harmless here because we only use it as a number, but it is
  why you should be careful naming locals after core functions."
  [{:keys [count average passing failing best]}]
  (str/join "\n"
            [(format "  students          %d" count)
             (format "  class average     %.2f" average)
             (format "  passing           %d" passing)
             (format "  failing           %d" failing)
             (format "  top of the class  %s" best)]))
