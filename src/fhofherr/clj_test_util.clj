(ns fhofherr.clj-test-util)

(defn wrap-test
  [f t]
  {:pre [(:test (meta t))]}
  (let [act-test (:test (meta t))
        wrapper (fn [] (f act-test))
        wrap (fn [cur-meta] (assoc cur-meta :test wrapper))]
    (vary-meta t wrap)))
