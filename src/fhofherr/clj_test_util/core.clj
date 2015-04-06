(ns fhofherr.clj-test-util.core)

(defn- noop
  [])

(defn- chain-fns
  [f1 f2]
  (fn []
    (f1)
    (f2)))

(defn fncall
  [f]
  (f))

(defn- comp-around-fns
  "Compose two around-each fixture functions.

  Returns a new around-each fixture function expecting a `test-fn`. The returned
  function creates a function which applies the `inner-fn` to the `test-fn`.
  This function is then passed to the `outer-fn`."
  [outer-fn inner-fn]
  (fn [test-fn]
    (let [new-test-fn #(inner-fn test-fn)]
      (outer-fn new-test-fn))))

(defn compose-fixtures
  [fixtures]
  (let [composers {:before-each chain-fns
                   :after-each chain-fns
                   :around-each comp-around-fns}
        do-compose (fn [acc [fixture-type f]]
                     (as-> acc $
                       (fixture-type $)
                       ((fixture-type composers) $ f)
                       (assoc acc fixture-type $)))]
    (->> fixtures
       (partition 2)
       (reduce do-compose {:before-each noop
                           :after-each noop
                           :around-each fncall}))))

(defn wrap-test
  [before-each around-each actual-test after-each]
  (fn []
    (before-each)
    (try
      (around-each actual-test)
      (finally
        (after-each)))))

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
