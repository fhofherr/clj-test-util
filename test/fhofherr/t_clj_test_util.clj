(ns fhofherr.t-clj-test-util
  (:require [clojure.test :refer :all]
            [fhofherr.clj-test-util :as test-util]))

(defn- register-call
  [calls kw]
  (fn [] (swap! calls conj kw)))

(deftest fixture-composition

  (testing "before-each fixture composition"
    (let [calls (atom [])
          fixtures [:before-each (register-call calls :before-each-1)
                    :before-each (register-call calls :before-each-2)]
          composed (test-util/compose-fixtures fixtures)]
      ((:before-each composed))
      (is (= [:before-each-1 :before-each-2] @calls))))
  )

(def fixture-calls (atom []))

(test-util/fixture
  before-each
  [:before-each (register-call fixture-calls :before-each-1)
   :before-each (register-call fixture-calls :before-each-2)]

  (deftest before-each-fixture
    (is (= [:before-each-1 :before-each-2] @fixture-calls))))
