(ns growmonster.core-test
  (:require [growmonster.core :as c]
            [clojure.test :refer :all]))

(deftest inflate-entities
  (let [shrunk [:mages
                [:xanax
                 {:name "xanax"
                  :id 1}

                 :vicodin
                 {:name "vicodin"
                  :id 2}]

                :spells
                [[{:author [:mages :xanax :id]}
                  {:name "drowsiness"
                   :id 10}
                  {:name "dizziness"
                   :id 11}]
                 [{:author [:mages :vicodin :id]}
                  {:name "nausea"
                   :id 20}
                  {:name "anxiety"
                   :id 21}]

                 {:name "paranoia"
                  :id 30}]]]
    (is (= [{:name "xanax" :id 1}
            {:name "vicodin" :id 2}
            {:name "drowsiness" :id 10 :author 1}
            {:name "dizziness"  :id 11 :author 1}
            {:name "nausea"     :id 20 :author 2}
            {:name "anxiety"    :id 21 :author 2}
            {:name "paranoia"   :id 30}]
           (c/inflatev shrunk)))))
