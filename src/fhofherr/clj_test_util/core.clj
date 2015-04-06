(ns fhofherr.clj-test-util.core
  (:require [fhofherr.clj-test-util.core.utils :refer :all]))

(defn- emit-definition-wrapper
  [definitions before-each around-each after-each]
  (for [definition definitions]
    `(let [test-var# ~definition
           test# (:test (meta test-var#))]
       (alter-meta! test-var#
                    assoc
                    :test (wrap-test
                            ~before-each
                            ~around-each
                            test#
                            ~after-each))
       test-var#)))

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
  (let [fixtures (gensym "fixtures")
        before-each (gensym "before-each")
        after-each (gensym "after-each")
        around-each (gensym "around-each")]
    `(let [~fixtures (compose-fixtures ~fixture-fns)
           ~before-each (:before-each ~fixtures)
           ~after-each (:after-each ~fixtures)
           ~around-each (:around-each ~fixtures)]
       ~@(emit-definition-wrapper definitions before-each around-each after-each))))