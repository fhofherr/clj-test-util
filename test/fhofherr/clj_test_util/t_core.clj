(ns fhofherr.clj-test-util.t-core
  (:require [clojure.test :refer :all]
            [fhofherr.clj-test-util.core :as test-util]
            [fhofherr.clj-test-util.core.utils :as utils]))

(deftest check-fulfillment-of-expectations

  (testing "use values as expectations"
    (is (true? (test-util/fulfills? 1 1)))
    (is (false? (test-util/fulfills? 1 2))))

  (testing "use functions as expectations"
    (is (true? (test-util/fulfills? #(= % 1) 1)))
    (is (false? (test-util/fulfills? #(= % 1) 2))))

  (testing "use collections as expectations"
    (is (true? (test-util/fulfills? [1] [1])))
    (is (true? (test-util/fulfills? (list 1) (list 1))))
    (is (true? (test-util/fulfills? (seq [1]) (seq [1]))))
    (is (false? (test-util/fulfills? [1] [2])))
    (is (false? (test-util/fulfills? (list 1) (list 2))))
    (is (false? (test-util/fulfills? (seq [1]) (seq [2]))))
    (is (true? (test-util/fulfills? {:key "value"} {:key "value"})))
    (is (true? (test-util/fulfills? {:key #(= % "value")} {:key "value"})))
    (is (false? (test-util/fulfills? {:key "value"} {:wrong-key "value"})))
    (is (false? (test-util/fulfills? {:key "value"} {:key "wrong-value"})))

    (is (true? (test-util/fulfills? #{:a :b} :a)))
    (is (true? (test-util/fulfills? #{[:a] [:b]} [:a])))
    (is (false? (test-util/fulfills? #{:a :b} :c)))
    (is (true? (test-util/fulfills? #{:a :b} #{:a :b})))
    (is (false? (test-util/fulfills? #{:a :b} #{:c :d})))

    (is (true? (test-util/fulfills? [#(= % 1)] [1])))
    (is (false? (test-util/fulfills? [#(= % 1)] [2]))))

  (testing "there may be less expectations than candidates"
    (is (true? (test-util/fulfills? [1] [1 2])))
    (is (false? (test-util/fulfills? [2] [1 2])))
    (is (true? (test-util/fulfills? {:key "value"}
                                    {:key "value" :another-key "something"}))))

  (testing "when comparing collections candidates must be a collection too"
    (is (thrown? IllegalArgumentException (test-util/fulfills? [1] 1)))
    (is (thrown? IllegalArgumentException (test-util/fulfills? {:key 1} 1))))

  (testing "there must not be less candidates than expectations"
    (is (thrown? IllegalArgumentException (test-util/fulfills? [1 2] [1])))
    (is (thrown? IllegalArgumentException (test-util/fulfills? {:key 2} {})))))

(deftest is-fulfilled
  (is (fulfilled? 1 1))
  (is (fulfilled? true? true))
  (is (fulfilled? [1 #(= % 2)] [1 2]))
  (is (fulfilled? {:key true?} {:key true})))

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
