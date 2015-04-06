(ns fhofherr.t-clj-test-util
  (:require [clojure.test :refer :all]
            [fhofherr.clj-test-util :as test-util]))

(defn- register-call
  ([calls kw]
   (fn [] (swap! calls conj kw)))

  ([calls before-kw after-kw]
   (fn [f]
     (swap! calls conj before-kw)
     (f)
     (swap! calls conj after-kw))))

(deftest fixture-composition

  (testing "before-each fixture composition"
    (let [calls (atom [])
          fixtures [:before-each (register-call calls :before-each-1)
                    :before-each (register-call calls :before-each-2)]
          composed (test-util/compose-fixtures fixtures)]
      ((:before-each composed))
      (is (= [:before-each-1 :before-each-2] @calls))))

  (testing "after-each fixture composition"
    (let [calls (atom [])
          fixtures [:after-each (register-call calls :after-each-1)
                    :after-each (register-call calls :after-each-2)]
          composed (test-util/compose-fixtures fixtures)]
      ((:after-each composed))
      (is (= [:after-each-1 :after-each-2]
             @calls))))

  (testing "around-each fixture composition"
    (let [calls (atom [])
          fixtures [:around-each (register-call calls
                                                :around-each-1-before
                                                :around-each-1-after)
                    :around-each (register-call calls
                                                :around-each-2-before
                                                :around-each-2-after)]
          composed (test-util/compose-fixtures fixtures)]
      ((:around-each composed) (fn [] nil))

      (is (= [:around-each-1-before
              :around-each-2-before
              :around-each-2-after
              :around-each-1-after]
             @calls)))))

(deftest wrap-test

  (testing "wrap tests in before and after"
    (let [calls (atom [])
          before-each (register-call calls :before-each)
          the-test (register-call calls :the-test)
          after-each (register-call calls :after-each)
          wrapped-test (test-util/wrap-test before-each
                                            test-util/fncall
                                            the-test
                                            after-each)]
      (wrapped-test)
      (is (= [:before-each :the-test :after-each] @calls))))

  (testing "after is executed even if the test fails"
    (let [calls (atom [])
          before-each (register-call calls :before-each)
          failing-test (fn [] (throw (Throwable. "Ooops!")))
          after-each (register-call calls :after-each)
          wrapped-test (test-util/wrap-test before-each
                                            test-util/fncall
                                            failing-test
                                            after-each)]
      (try
        (wrapped-test)
        (catch Throwable _))
      (is (= [:before-each :after-each] @calls)))))

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
