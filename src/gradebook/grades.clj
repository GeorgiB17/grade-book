(ns gradebook.grades
  "Every function here is PURE: give it the same students, it gives you the
  same answer, and it never changes anything you passed in.")

;; ---------------------------------------------------------------------------
;; Adding up a list, three ways
;; ---------------------------------------------------------------------------

(defn sum
  "The idiomatic version. `reduce` folds a collection down to one value: it
  starts at 0 and calls (+ running-total next-number) for each element."
  [numbers]
  (reduce + 0 numbers))

(defn sum-recursive
  "The same thing by hand. Readable, but every call has to wait for the one
  below it to come back so it can do the `+`. Each waiting call takes a stack
  frame, so a long enough list crashes with a StackOverflowError."
  [numbers]
  (if (empty? numbers)
    0
    (+ (first numbers)
       (sum-recursive (rest numbers)))))

(defn sum-loop
  "The same thing again with `loop`/`recur`.

  `loop` marks a spot to jump back to; `recur` jumps there with new values,
  REUSING the same stack frame instead of adding one. So this never overflows.

  The catch: `recur` is only allowed in TAIL position — it must be the very
  last thing the function does. That is exactly why `sum-recursive` above
  cannot use it: after the recursive call there is still a `+` waiting."
  [numbers]
  (loop [remaining numbers
         total     0]
    (if (empty? remaining)
      total
      (recur (rest remaining)
             (+ total (first remaining))))))

;; ---------------------------------------------------------------------------
;; One student
;; ---------------------------------------------------------------------------

(defn average
  [numbers]
  (if (empty? numbers)
    0.0
    (double (/ (sum numbers) (count numbers)))))

(defn student-average
  "`(:scores student)` — a keyword is a function that looks itself up in a map."
  [student]
  (average (:scores student)))

(defn letter-grade
  "`cond` tries each test in order and returns the first match.
  `:else` is not special — it is just a keyword, and every keyword is truthy,
  so it always matches. It is the conventional way to write a default."
  [score]
  (cond
    (>= score 90) "A"
    (>= score 80) "B"
    (>= score 70) "C"
    (>= score 60) "D"
    :else         "F"))

(defn grade-of
  [student]
  (letter-grade (student-average student)))

(defn passing?
  "By convention a function returning true/false ends in a question mark."
  [student]
  (>= (student-average student) 60))

(defn add-score
  "Give a student one more score.

  This does NOT change the student you passed in. `update` returns a brand new
  map with `:scores` replaced by the result of `(conj old-scores score)`.
  There is a test proving the original is untouched."
  [student score]
  (update student :scores conj score))

;; ---------------------------------------------------------------------------
;; The whole class
;; ---------------------------------------------------------------------------

(defn passing-students
  [students]
  (filter passing? students))

(defn failing-students
  "`remove` is `filter` with the test flipped."
  [students]
  (remove passing? students))

(defn ranked
  "Best average first.

  The sort key is a VECTOR. Clojure compares vectors element by element, so
  this means 'by average descending (hence the minus), then by name' — the
  tie-break keeps the output identical every single run."
  [students]
  (sort-by (fn [student] [(- (student-average student)) (:name student)])
           students))

(defn top
  [students n]
  (take n (ranked students)))

(defn class-average
  [students]
  (average (map student-average students)))

(defn grade-distribution
  "How many A's, B's, and so on. `frequencies` is built into Clojure."
  [students]
  (frequencies (map grade-of students)))

(defn by-group
  "`group-by` builds a map from a key to the vector of items with that key.
  Passing the keyword `:group` directly works because keywords are functions."
  [students]
  (group-by :group students))

(defn group-averages
  "[[:evening 74.42] [:morning 76.33]]

  `->>` puts the value in as the LAST argument of each following form, which
  turns inside-out nesting into a readable pipeline. It is only syntax — try
  `(macroexpand '(->> x f (g 1)))` and you will see plain nested calls."
  [students]
  (->> students
       by-group
       (map (fn [[group members]]                    ; destructure each pair
              [group (class-average members)]))
       (sort-by first)))

(defn add-score-to
  "Give one named student a score, and hand back a NEW class list.

  `mapv` is `map` that returns a vector. Everyone who is not the named student
  is passed through unchanged — and 'unchanged' here is literal: it is the very
  same map object, not a copy."
  [students student-name score]
  (mapv (fn [student]
          (if (= (:name student) student-name)
            (add-score student score)
            student))
        students))

(defn summary
  [students]
  {:count        (count students)
   :average      (class-average students)
   :passing      (count (passing-students students))
   :failing      (count (failing-students students))
   :best         (:name (first (ranked students)))
   :distribution (grade-distribution students)})
