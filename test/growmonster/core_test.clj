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

(def complex-fixture
  [:categories
   [:alcohols
    {:category/name "Alcohols"}]

   :tags
   [[{:tag/category [:categories :alcohols :category/name]}
     :gin
     {:tag/name "Gin"}

     :bourbon
     {:tag/name "Bourbon"}]]

   :recipes
   [{:recipe/name "Ginger Collins"
     :recipe/ingredients [{:ingredient/tag [:tags :gin :tag/name]}
                          [:tags :gin :tag/name]]
     :recipe/tags [[:tags :gin :tag/name]
                   [:tags :bourbon :tag/name]]}]])

(deftest complex
  ;; Nested references, vectors of references, and post-inflate
  ;; transform
  (is (= [{:category/name "Alcohols"}
          {:tag/category "Alcohols"
           :tag/name "gin"}
          {:tag/category "Alcohols"
           :tag/name "bourbon"}
          {:recipe/name "Ginger Collins"
           :recipe/ingredients [{:ingredient/tag "gin"} "gin"]
           :recipe/tags ["gin" "bourbon"]}]
         (c/inflatev complex-fixture
                     #(if (:tag/name %)
                        (update % :tag/name clojure.string/lower-case)
                        %)))))
