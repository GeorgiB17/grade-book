(ns gradebook.debug
 )

(defmacro spy
  "Print an expression AND its value, then return the value.

      (spy (+ 1 2))
      spy: (+ 1 2) => 3
      ;=> 3
   "
  [expression]
  `(let [value# ~expression]
     (println "  spy:" '~expression "=>" value#)
     value#))
