(ns fhofherr.clj-test-util.core.t-utils
  (:require [clojure.test :refer :all]
            [fhofherr.clj-test-util.core.utils :as utils]))

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
          composed (utils/compose-fixtures fixtures)]
      ((:before-each composed))
      (is (= [:before-each-1 :before-each-2] @calls))))

  (testing "after-each fixture composition"
    (let [calls (atom [])
          fixtures [:after-each (register-call calls :after-each-1)
                    :after-each (register-call calls :after-each-2)]
          composed (utils/compose-fixtures fixtures)]
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
          composed (utils/compose-fixtures fixtures)]
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
          wrapped-test (utils/wrap-test before-each
                                            utils/fncall
                                            the-test
                                            after-each)]
      (wrapped-test)
      (is (= [:before-each :the-test :after-each] @calls))))

  (testing "after is executed even if the test fails"
    (let [calls (atom [])
          before-each (register-call calls :before-each)
          failing-test (fn [] (throw (Throwable. "Ooops!")))
          after-each (register-call calls :after-each)
          wrapped-test (utils/wrap-test before-each
                                            utils/fncall
                                            failing-test
                                            after-each)]
      (try
        (wrapped-test)
        (catch Throwable _))
      (is (= [:before-each :after-each] @calls)))))
