(set-env!
 :source-paths   #{"src"}
 :resource-paths #{}
 :target-path    "target/build"
 :dependencies   '[[org.clojure/clojure      "1.7.0"    :scope "provided"]
                   [boot/core                "2.5.5"    :scope "provided"]
                   [adzerk/boot-test         "1.1.1"    :scope "test"]
                   [adzerk/bootlaces         "0.1.13"   :scope "test"]])

(require '[adzerk.bootlaces :refer :all]
         '[adzerk.boot-test :refer :all])


(def +version+ "0.1.0")
(bootlaces! +version+)

(task-options!
 pom  {:project     'growmonster
       :version     +version+
       :description "Transform compact fixtures into a seq of full records"
       :url         "https://github.com/flyingmachine/growmonster"
       :scm         {:url "https://github.com/flyingmachine/growmonster"}
       :license     {"MIT" "https://opensource.org/licenses/MIT"}})

(deftask testenv []
  (set-env! :source-paths #{"test"})
  identity)
