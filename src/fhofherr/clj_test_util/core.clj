(ns fhofherr.clj-test-util.core
  (:require [clojure.test :refer [assert-expr
                                  do-report
                                  function?]]
            [fhofherr.clj-test-util.core.utils :refer :all]))

(defmulti fulfills?
  "Check if a `candidate` fulfills an `expectation`.

  Expectations can be given as values, functions, or collections

  Values will be compared using `=`.

  Functions are assumed to be predicates. They are applied to the `candidate`.
  If the function returns `true` the `candidate` fulfills the expectation.

  If a collection is given as `expectation` it is treated as a collection of
  expectations. In this case `candidate` must be a collection, too. For
  sequential collections of expectations each expectation is checked against
  the candidate value at the same position of the candidate collection. If
  `expectation` is a map expectations are checked against candidates with the
  same key. If there are more candidates than expectations, addiditional
  candidates are ignored. However, there may not be more expectations than
  candidates.

  Returns `true` if the `candidate` fulfills the `expectation` and `false` if
  it does not."
  (fn [expectation candidate]
    (when (and
            (coll? expectation)
            (coll? candidate)
            (< (count candidate) (count expectation)))
      (throw (IllegalArgumentException.
               "There must not be less candidates than expectations")))
    (cond
      (function? expectation) ::predicate
      (sequential? expectation) ::sequential
      (map? expectation) ::map
      :else (class expectation)))
  :default ::default)

(defmethod fulfills? ::predicate
  [expectation candidate]
  (expectation candidate))

(defmethod fulfills? ::sequential
  [expectation candidate]
  (when (not (sequential? candidate))
    (throw (IllegalArgumentException.
             "Can't compare sequential expectations to non-sequential candidates!")))
  (every? true? (map fulfills? expectation candidate)))

(defmethod fulfills? ::map
  [expectation candidate]
  (when (not (map? candidate))
    (throw (IllegalArgumentException.
             "Can't compare map expectations to non-map candidates!")))
  (every? true? (for [[k e] expectation]
                  (fulfills? e (get candidate k ::missing-candidate-value)))))

(defmethod fulfills? ::default
  [expectation candidate]
  (= expectation candidate))

(defmethod assert-expr 'fulfilled?
  [msg form]
  (let [[_ expectation-form candidate-form] form]
    `(if (fulfills? ~expectation-form ~candidate-form)
       (do-report {:type :pass :message ~msg})
       (do-report {:type :fail
                   :message ~msg
                   :expected '~form
                   :actual '~(list 'not form)}))))

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
