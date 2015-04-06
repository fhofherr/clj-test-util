(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [clojure.test :as t]
            [clojure.repl :refer [doc find-doc source]]
            [clojure.java.javadoc :refer [javadoc]]))

(defn- do-test
  []
  (t/run-all-tests #"fhofherr\.clj-test-util\.([^.]+\.)?t-.+"))

(defn refresh-test
  []
  (repl/refresh :after 'user/do-test))
