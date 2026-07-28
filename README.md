# Gradebook

A class gradebook in Clojure. It holds a list of students, works out their
averages and letter grades, ranks them, groups them, and prints a report.


## Run it

```bash
clj -M -m gradebook.core        # print the report
clj -M -m gradebook.core-test   # run the tests
clj -M -r                       # REPL
```

## The data

That's the whole model — a vector of maps:

```clojure
(def students
  [{:name "Ada"   :group :morning :scores [92 88 95]}
   {:name "Bruno" :group :morning :scores [55 61 48]}
   ...])
```

No classes, no objects, no constructors. Just data you can read with your eyes.

## What it prints

```
Class list (best first)
-----------------------
  NAME     GROUP      AVG  GRADE
  Georgi     evening   96.33    A
  Ada      morning   91.67    A
  Eli      morning   87.67    B
  Chen     evening   80.00    B
  Goran    morning   71.33    C
  Dara     evening   69.67    D
  Bruno    morning   54.67    F   <- failing
  Fatima   evening   51.67    F   <- failing

Summary
-------
  students          8
  class average     75.38
  passing           6
  failing           2
  top of the class  Georgi

Grade distribution
------------------
  A   2  ██
  B   2  ██
  C   1  █
  D   1  █
  F   2  ██
```

...then it demonstrates that nothing was ever modified, and shows the `spy`
macro in action.

## The files

| File | Lines | Job |
|---|---|---|
| `data.clj` | 15 | the students |
| `grades.clj` | 160 | all the calculations |
| `report.clj` | 65 | results → strings (prints nothing) |
| `debug.clj` | 35 | one macro, `spy` |
| `core.clj` | 55 | wires it together; the only place that prints |

