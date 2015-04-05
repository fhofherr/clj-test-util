(ns fhofherr.t-clj-test-util
  (:require [clojure.test :refer :all]
            [fhofherr.clj-test-util :as test-util]))

;; Since we write helper functions that manipulate clojure.test tests we
;; need some test to manipulate. It does not have any meaning of itself.
(defn create-some-test
  [tracker sig-kw]
  (with-meta (fn [] :something)
             {:test (fn []
                      (swap! tracker conj sig-kw)
                      (is (= 1 1)))}))

(deftest wrap-test-in-function
  (let [tracker (atom [])
        f (fn [t] (t))
        wrapped-test (test-util/wrap-test f (create-some-test tracker
                                                              :test-called))]
    (test wrapped-test)
    (is (= @tracker [:test-called]))))
