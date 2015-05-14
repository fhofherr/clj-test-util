(ns fhofherr.clj-test-util.t-core
  (:require [clojure.test :refer :all]
            [fhofherr.clj-test-util.core :as test-util]
            [fhofherr.clj-test-util.core.utils :as utils]))

;; The following test test the fixture macro. Since the fixture
;; macro modifies the test functions created by deftest we definie
;; dummy tests within the fixture macro. Those dummy tests are then
;; called from tests outside of the fixture macro.

(defn- register-call
  ([calls kw]
   (fn [] (swap! calls conj kw)))

  ([calls before-kw after-kw]
   (fn [f]
     (swap! calls conj before-kw)
     (f)
     (swap! calls conj after-kw))))

(def before-each-calls (atom []))
(test-util/fixture
  before-each-dummy
  [:before-each (register-call before-each-calls :before-each-1)
   :before-each (register-call before-each-calls :before-each-2)]

  (deftest before-each-fixture-prep
    (is (= 1 1))))

(deftest before-each-fixture
  (reset! before-each-calls [])
  (before-each-fixture-prep)
  (is (= [:before-each-1 :before-each-2] @before-each-calls)))

(def after-each-calls (atom []))
(test-util/fixture
  after-each-dummy
  [:after-each (register-call after-each-calls :after-each-1)
   :after-each (register-call after-each-calls :after-each-2)]
  (deftest after-each-fixture-prep
    (is (= 1 1))))

(deftest after-each
  ;; Call the after-each-fixture-prep test from above.
  (reset! after-each-calls [])
  (after-each-fixture-prep)

  (is (= [:after-each-1 :after-each-2] @after-each-calls)))

(def around-each-calls (atom []))
(test-util/fixture
  around-each-dummy
  [:around-each (register-call around-each-calls
                               :before-around-each-1
                               :after-around-each-1)
   :around-each (register-call around-each-calls
                               :before-around-each-2
                               :after-around-each-2)]
  (deftest around-each-fixture-prep
    (is (= 1 1))))

(deftest around-each
  (reset! around-each-calls [])
  (around-each-fixture-prep)
  (is (= [:before-around-each-1
          :before-around-each-2
          :after-around-each-2
          :after-around-each-1]
         @around-each-calls)))
