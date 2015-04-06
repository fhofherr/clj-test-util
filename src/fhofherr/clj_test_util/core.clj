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
  "Create a test fixture for the given `definitions`.

  The bindings vector may contain `:before-each`, `:after-each` and
  `:around-each` fixture functions. The `:before-each` functions will be called
  before each test in `definitions`. The `:after-each` functions will be called
  after each test in `definitions`. The `:around-each` functions will be called
  around each test in `definitions`. If multiple `:before-each`, `:after-each`,
  or `:around-each` functions are given they will be called in the order they
  are listed."
  [fixture-name bindings & definitions]
  ;; TODO IllegalArgumnentException
  (assert (even? (count bindings))
          "fixture requires an even number of definitions in bindings")
  (let [fixtures (gensym "fixtures")
        before-each (gensym "before-each")
        after-each (gensym "after-each")
        around-each (gensym "around-each")]
    `(let [~fixtures (compose-fixtures ~bindings)
           ~before-each (:before-each ~fixtures)
           ~after-each (:after-each ~fixtures)
           ~around-each (:around-each ~fixtures)]
       ~@(emit-definition-wrapper definitions before-each around-each after-each))))
