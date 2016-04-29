(ns growmonster.core)

(defn mapval
  [f m]
  (if (empty? m)
    m
    (reduce into (map (fn [[k v]] {k (f k v)}) m))))

(defn replace-in
  [smap rmap]
  (mapval (fn [k v] (if (sequential? v) (get-in smap v) v))
          rmap))

(defn inflate
  "returns map and vector of inflated data"
  [data]
  (reduce
   (fn [inflated [group-name ents]]
     (loop [inflated-map (first inflated)
            inflated-vec (second inflated)
            ent-name     nil
            ents         ents]
       (if (empty? ents)
         [inflated-map inflated-vec]
         (let [[head & tail] ents]
           (cond (vector? head)
                 (recur inflated-map
                        inflated-vec
                        nil
                        (into (mapv #(if (map? %)
                                       (merge (first head) %)
                                       %)
                                    (rest head))
                              tail))
                 
                 (keyword? head)
                 (recur inflated-map
                        inflated-vec
                        head
                        tail)

                 :else
                 (let [inflated-ent (replace-in inflated-map head)]
                   (recur (if ent-name
                            (assoc-in inflated-map [group-name ent-name] inflated-ent)
                            inflated-map)
                          (conj inflated-vec inflated-ent)
                          nil
                          tail)))))))
   [{} []]
   (partition-all 2 data)))

(defn inflatev
  [data]
  (second (inflate data)))

(defn inflatem
  [data]
  (first (inflate data)))
