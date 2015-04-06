(ns fhofherr.clj-test-util)

(defmacro fixture
  "TODO: multiple before each fixtures ==> in definition order
  TODO: multiple after each fixtures ==> in definition order
  TODO: multiple arount each fixtures ==> in definition order
  TODO: mixture of before, after, around each ==> before-each first,
  then around-each, then after-each
  TODO: after each even if around hook, or test itself fail

  TODO: be carefull if befor each fails, nothing else will be executed (danger
  if using multiple before hooks)
  "
  [fixture-name fixture-fns & definitions]
  ;; TODO IllegalArgumnentException
  (assert (even? (count fixture-fns))
          "fixture requires an even number of definitions in fixture-fns!")
  (let [before-fixture (gensym "before-fixture")]
    `(let [~before-fixture ~(second fixture-fns)]
       ~@(for [definition definitions]
           `(let [test-var# ~definition
                  test# (:test (meta test-var#))]
              (alter-meta! test-var#
                           assoc
                           :test (fn [] (~before-fixture) (test#)))
              test-var#)
           )
       )))
