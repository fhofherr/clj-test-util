(ns fhofherr.t-clj-test-util
  (:require [clojure.test :refer :all]
            [fhofherr.clj-test-util :as test-util]))

(defn- register-call
  [calls kw]
  (fn [] (swap! calls conj kw)))

(def fixture-calls (atom []))
(test-util/fixture
  before-each
  [:before-each (register-call fixture-calls :before-each-called)]

  (deftest before-each-fixture
    (is (= [:before-each-called] @fixture-calls))))
