(ns fhofherr.clj-test-util.core.utils)

(defn noop
  "No operation."
  [])

(defn chain-fns
  "Create a no-argument function that calls `f1` first and then `f2`.
  The return value of `f1` is discarded."
  [f1 f2]
  (fn []
    (f1)
    (f2)))

(defn fncall
  "Call the function `f`."
  [f]
  (f))

(defn comp-around-fns
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
