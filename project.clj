(defproject fhofherr/clj-test-util "0.1.0-SNAPSHOT"
  :description "Helpers and utilities for clojure test"
  :url "https://github.com/fhofherr/clj-test-util"
  :scm {:name "git"
        :url "https://github.com/fhofherr/clj-test-util"}
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[codox "0.8.12"]]
  :codox {:src-dir-uri "https://github.com/fhofherr/clj-test-util/blob/master/"
          :src-linenum-anchor-prefix "L"
          :output-dir "target/codox"
          :exclude [user]
          :defaults {:doc/format :markdown}}
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.10"]]}})
