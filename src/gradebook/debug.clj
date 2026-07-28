(ns gradebook.debug
  "One macro. It is here because a macro is the only thing that can do this job.")

(defmacro spy
  "Print an expression AND its value, then return the value.

      (spy (+ 1 2))
      spy: (+ 1 2) => 3
      ;=> 3

  This CANNOT be a function. A function is handed the value `3`; the code
  `(+ 1 2)` is gone by the time it runs, so it has nothing to print. A macro is
  handed the unevaluated code itself — `(+ 1 2)` arrives as a three-element
  list — so it can both print it and use it.

  Because it returns the value, you can drop it into the middle of a pipeline
  to see what is flowing through, then delete it again."
  [expression]
  `(let [value# ~expression]
     (println "  spy:" '~expression "=>" value#)
     value#))

;; How to read the syntax above:
;;
;;   `    syntax quote  -- "build this as code rather than running it"
;;   ~    unquote       -- "...but here, drop in the thing I was given"
;;   '~x  quote+unquote -- "drop in the code I was given, and keep it as data"
;;   x#   auto-gensym   -- "invent a name nobody else could have"
;;
;; `value#` becomes something like value__1042__auto__, so if the caller happens
;; to have their own local named `value`, this macro cannot clobber it. Macros
;; that are safe in that way are called hygienic.
;;
;; See the expansion for yourself:
;;   (macroexpand '(spy (+ 1 2)))
