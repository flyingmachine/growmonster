(ns growmonster.core)

(defn mapval
  [f m]
  (if (empty? m)
    m
    (reduce into (map (fn [[k v]] {k (f k v)}) m))))


(defprotocol InflateEnt
  (inflate-ent [ent inflated-map post-inflate] "inflate with post-inflate"))

(defn inflate-map
  [ent inflated-map post-inflate]
  (->> ent
       (mapval #(inflate-ent %2 inflated-map post-inflate))
       post-inflate))

(extend-protocol InflateEnt
  clojure.lang.PersistentVector
  (inflate-ent [ent inflated-map post-inflate]
    (if (keyword? (first ent))
      (get-in inflated-map ent)
      (mapv #(inflate-ent % inflated-map post-inflate) ent)))

  clojure.lang.PersistentArrayMap
  (inflate-ent [ent inflated-map post-inflate]
    (inflate-map ent inflated-map post-inflate))

  clojure.lang.PersistentHashMap
  (inflate-ent [ent inflated-map post-inflate]
    (inflate-map ent inflated-map post-inflate))

  java.lang.Object
  (inflate-ent [ent inflated-map post-inflate] ent)

  nil
  (inflate-ent [ent inflated-map post-inflate] ent))

(defn inflate
  "returns map and vector of inflated data"
  ([data] (inflate data identity))
  ([data post-inflate]
   (reduce
    (fn [inflated [group-name ents]]
      (loop [inflated-map (first inflated)
             inflated-vec (second inflated)
             ent-name     nil
             ents         ents]
        (if (empty? ents)
          [inflated-map inflated-vec]
          (let [[head & tail] ents]
            (cond (vector? head) ;; merge a common map
                  (recur inflated-map
                         inflated-vec
                         nil
                         (let [[common & ents] head]
                           (into (mapv #(if (map? %)
                                          (merge common %)
                                          %)
                                       ents)
                                 tail)))
                  
                  (keyword? head)
                  (recur inflated-map
                         inflated-vec
                         head
                         tail)

                  :else ; head is a map representing an entity
                  (let [inflated-ent (inflate-ent head inflated-map post-inflate)]
                    (recur (if ent-name
                             (assoc-in inflated-map [group-name ent-name] inflated-ent)
                             inflated-map)
                           (conj inflated-vec inflated-ent)
                           nil
                           tail)))))))
    [{} []]
    (partition-all 2 data))))

(defn inflatev
  ([data]
   (inflatev data identity))
  ([data post-inflate]
   (second (inflate data post-inflate))))

(defn inflatem
  ([data]
   (inflatem data identity))
  ([data post-inflate]
   (first (inflate data post-inflate))))
