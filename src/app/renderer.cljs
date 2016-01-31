(ns app.renderer
  (:require [cljsjs.fabric]))

; (def log js/console.log)

(def app-state (atom {}))

;; * FABRIC
(def grid-x (- (/ (.-innerWidth js/window) 2) 250))
(def grid-y (- (/ (.-innerHeight js/window) 2) 290))

(def f js/fabric)
(def canvas (f.Canvas. "canvas-id" #js {:renderOnAddRemove false}))


;; * GRAPHICS OPERATIONS
(declare solids)
(declare make-rect)
(declare set-white)
(declare set-black)

(defn make-rect [x y]
  (f.Rect. #js {:width 19 :height 19 :fill "white"
                :hasControls false
                :selectable false
                :lockMovementX true
                :lockMovementy true
                :left (+ grid-x (* (dec x) 20) 1)
                :top (+ grid-y (* (dec y) 20) 1)}))

(def rect-matrix
  (for [y (range 1 26) x (range 1 26)]
    (make-rect x y)))

(defn set-white [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "white")
  (.renderAll canvas))

(defn set-white-1 [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "white"))
  
(defn set-black [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "black")
  (.renderAll canvas))

(defn set-black-1 [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "black"))

(defn set-red-1 [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "#ffd8d8"))
;; #ffb4c7
;; #ffd8d8

;;(doseq [rect (map #(apply set-black %) solids)])


;; * GCHQ ALGS

(def solids-m {[22 22] :s, [16 22] :s, [22 4] :s, [7 9] :s, [10 22] :s, [11 9] :s, [12 17] :s, [19 9] :s, [17 17] :s, [15 9] :s, [4 22] :s, [21 17] :s, [14 4] :s, [16 9] :s, [7 17] :s, [5 4] :s, [5 22] :s, [21 22] :s, [4 4] :s, [13 4] :s, [8 9] :s, [11 22] :s})

(def rules {:x [        [7 2 1 1 7]       [1 1 2 2 1 1] [1 3 1 3 1 3 1 3 1] [1 3 1 1 5 1 3 1] [1 3 1 1 4 1 3 1]
                      [1 1 1 2 1 1]     [7 1 1 1 1 1 7]             [1 1 3]   [2 1 2 1 8 2 1] [2 2 1 2 1 1 1 2]
                        [1 7 3 2 1]   [1 2 3 1 1 1 1 1]         [4 1 1 2 6]   [3 3 1 1 1 3 1]       [1 2 5 2 2]
                [2 2 1 1 1 1 1 2 1]     [1 3 3 2 1 8 1]             [6 2 1]     [7 1 4 1 1 3]       [1 1 1 1 4]
                      [1 3 1 3 7 1] [1 3 1 1 1 2 1 1 4]       [1 3 1 4 3 3]   [1 1 2 2 2 6 1]     [7 1 3 2 1 1]]
            :y [  [7 3 1 1 7]     [1 1 2 2 1 1] [1 3 1 3 1 1 3 1] [1 3 1 1 6 1 3 1] [1 3 1 5 2 1 3 1]
                  [1 1 2 1 1]   [7 1 1 1 1 1 7]             [3 3] [2 3 1 1 3 1 1 2]     [1 1 3 2 1 1]
                [4 1 4 2 1 2] [1 1 1 1 1 4 1 3]     [2 1 1 1 2 5]     [3 2 2 6 3 1]     [1 9 1 1 2 1]
                [2 1 2 2 3 1]   [3 1 1 1 1 5 1]         [1 2 2 5]   [7 1 2 1 1 1 3]   [1 1 2 1 2 2 1]
                [1 3 1 4 5 1]    [1 3 1 3 10 2]     [1 3 1 1 6 6]     [1 1 2 1 1 2]       [7 2 1 2 5]]})


;; GRIDS OPERATIONS

(def grid-m (apply merge (for [x (range 1 26) y (range 1 26)] {[x y] :e})))
;; (simple-benchmark [] (get grid-m [3 3]) 1000000)
;; [], (get grid-m [3 3]), 1000000 runs, 1673 msecs

(defn draw-grid-m [grid]
  (doseq [e grid]
    (condp = (second e)
      :e (apply set-white-1 (first e))
      :s (apply set-black-1 (first e))
      (js/console.log (str "error value to draw in " (first e)))))
  (.renderAll canvas))

(def grid-ms (merge grid-m solids-m))

(defn get-row-m [n]
  (vals (sort (into {} (filter #(= (second (key %)) n) grid-ms)))))
;; (simple-benchmark [] (get-row-m 4) 10000)
;; [], (get-row-m 4), 10000 runs, 25738 msecs
;; BAD !!!!!

;; (draw-grid-m grid-ms)

(def grid-v (repeatedly 25 #(repeatedly 25 (constantly :e))))
;; (simple-benchmark [] (get (get grid-v 3) 3) 1000000)
;; [], (get (get grid-v 3) 3), 1000000 runs, 429 msecs

(def solids-v [[4  4] [ 5  4] [13  4] [14  4] [22  4]
               [7  9] [ 8  9] [11  9] [15  9] [16  9] [19  9]
               [7 17] [12 17] [17 17] [21 17]
               [4 22] [ 5 22] [10 22] [11 22] [16 22] [21 22] [22 22]])

(def solids-s #{[4  4] [ 5  4] [13  4] [14  4] [22  4]
                [7  9] [ 8  9] [11  9] [15  9] [16  9] [19  9]
                [7 17] [12 17] [17 17] [21 17]
                [4 22] [ 5 22] [10 22] [11 22] [16 22] [21 22] [22 22]})

(def grid-v1 (into [] (repeatedly 25 #(into [] (repeatedly 25 (constantly :e))))))
;; (simple-benchmark [] (get (get grid-v1 3) 3) 1000000)
;; [], (get (get grid-v1 3) 3), 1000000 runs, 328 msecs

;; (replicate 8 :s)
;; (:s :s :s :s :s :s :s :s)

;; (defn row-m [n m] (filter #(= n (second %)) m))
;; (defn row [n] (filter #(= n (second %)) solids))
;; (defn column-m [n m] (filter #(= n (first %)) m))
;; (defn column [n] (filter #(= n (first %)) solids))

;; (assoc-in grid-v1 [0 0] :s)
(defn get-row-v1 [n g]
  (g n))
;; (simple-benchmark [] (get-row-v1 4 grid-vs1) 10000)
;; [], (get-row-v1 4 grid-vs1), 10000 runs, 3 msecs

(defn get-col-v1 [n g]
  (map #(get % 4) g))
;; (simple-benchmark [] (get-col-v1 4 grid-vs1) 10000)
;; [], (get-col-v1 4 grid-vs1), 10000 runs, 10 msecs

(defn apply-solids-v1 []
  (loop [g grid-v1 i 0]
    (if (= i (dec (count solids-v))) g
        (recur (assoc-in g [(dec (second (solids-v i))) (dec (first (solids-v i)))] :s) (inc i)))))
;; (simple-benchmark [] ((apply-solids-v1) 0) 10000)
;; [], ((apply-solids-v1) 0), 10000 runs, 307 msecs

(defn apply-solids-v2 [s]
  (areduce (clj->js s) i g grid-v1
           (assoc-in g [(second (s i)) (first (s i))] :s)))
;; (simple-benchmark [] ((apply-solids-v2 solids-v) 4) 10000)
;; [], ((apply-solids-v2 solids-v) 4), 10000 runs, 1853 msecs

(defn draw-grid-v1 [grid]
  (doseq [x1 (range 25) y1 (range 25)
          :let [v ((grid y1) x1) x (inc x1) y (inc y1)]]
    (condp = v
      :e (set-white-1 x y)
      :s (set-black-1 x y)
      :i (set-red-1 x y)))
  (.renderAll canvas))

;; * RULES OPERATIONS
;; Verify that all rules have maximum potential 25 spaces
(defn validate-rules [k]
  (if (empty (filter #(> % 25)
                   (for [rule (k rules)]
                     (reduce + (interpose 1 rule)))))
    (do (js/console.log (str "rules for " (name k) " valid"))
        true)
    (js/console.log (str "rules for " (name k) " invalid, has more then 25 chars"))))

;; (defn index-rule-x [] (map-indexed (fn [id it] [(inc id) it]) (:x rules)))
;; (defn index-rule-y [] (map-indexed (fn [id it] [(inc id) it]) (:y rules)))
;; ([1 [7 3 1 1 7]] [2 [1 1 2 2 1 1]] [3 [1 3 1 3 1 1 3 1]] [4 [1 3 1 1 6 1 3 1]] [5 [1 3 1 5 2 1 3 1]] [6 [1 1 2 1 1]] [7 [7 1 1 1 1 1 7]] [8 [3 3]] [9 [2 3 1 1 3 1 1 2]] [10 [1 1 3 2 1 1]] [11 [4 1 4 2 1 2]] [12 [1 1 1 1 1 4 1 3]] [13 [2 1 1 1 2 5]] [14 [3 2 2 6 3 1]] [15 [1 9 1 1 2 1]] [16 [2 1 2 2 3 1]] [17 [3 1 1 1 1 5 1]] [18 [1 2 2 5]] [19 [7 1 2 1 1 1 3]] [20 [1 1 2 1 2 2 1]] [21 [1 3 1 4 5 1]] [22 [1 3 1 3 10 2]] [23 [1 3 1 1 6 6]] [24 [1 1 2 1 1 2]] [25 [7 2 1 2 5]])
(defn index-rule-1 [axis] (into [] (map-indexed (fn [id it] [(inc id) it]) ((keyword axis) (:rules @app-state)))))
(defn index-rule-2 [axis] (into [] (map-indexed (fn [id it] [(inc id) it]) ((keyword axis) rules))))
(defn index-rule-3 [rule] (map-indexed (fn [id it] [id it]) rule))

(defn reset-rules! []
  (swap! app-state :rules raw-rules))

(defn full-rule? [rule]
  (= 25 (reduce + (interpose 1 rule))))

;; (filter #(full-rule? (second %)) (:x (:rules @app-state)))
;; ([3 [1 3 1 3 1 3 1 3 1]] [7 [7 1 1 1 1 1 7]] [17 [1 3 3 2 1 8 1]])

(defn find-full-x []
  (map second (filter #(= (first %) 25)
                (for [rule (:x rules)]
                  [(reduce + (interpose 1 rule)) rule]))))
;; ([1 3 1 3 1 3 1 3 1] [7 1 1 1 1 1 7] [1 3 3 2 1 8 1])

;; (filter #(full-rule? (second %)) (index-rule-1 "x"))
;; ([3 [1 3 1 3 1 3 1 3 1]] [7 [7 1 1 1 1 1 7]] [17 [1 3 3 2 1 8 1]])

(defn expand-rule [rule]
  (->> rule
       (interpose :i)
       (map #(if (> % 0) (map (constantly :s) (range %)) %))
       (flatten)
       (into [])))
;; (expand-rule [7 1 1 1 1 1 7])
;; (1 1 1 1 1 1 1 0 1 0 1 0 1 0 1 0 1 0 1 1 1 1 1 1 1)

;; (map #(vector (first %) (expand-rule (second %))) (filter #(full-rule? (second %)) (index-rule-1 "x")))
(defn indexed-full-rules [axis]
  (map #(vector (first %) (expand-rule (second %)))
    (filter #(full-rule? (second %))
      (index-rule-1 (keyword axis)))))
;; ([3 (1 0 1 1 1 0 1 0 1 1 1 0 1 0 1 1 1 0 1 0 1 1 1 0 1)] [7 (1 1 1 1 1 1 1 0 1 0 1 0 1 0 1 0 1 0 1 1 1 1 1 1 1)] [17 (1 0 1 1 1 0 1 1 1 0 1 1 0 1 0 1 1 1 1 1 1 1 1 0 1)])
;; (indexed-full-rules "x")
;; ([3 (:s :i :s :s :s :i :s :i :s :s :s :i :s :i :s :s :s :i :s :i :s :s :s :i :s)] [7 (:s :s :s :s :s :s :s :i :s :i :s :i :s :i :s :i :s :i :s :s :s :s :s :s :s)] [17 (:s :i :s :s :s :i :s :s :s :i :s :s :i :s :i :s :s :s :s :s :s :s :s :i :s)])

(defn expand-x [x r] (map-indexed (fn [id it] [it x (inc id)]) r))
;; (expand-x 7 (expand-rule [7 1 1 1 1 1 7]))
;; ([7 1 1] [7 2 1] [7 3 1] [7 4 1] [7 5 1] [7 6 1] [7 7 1] [7 8 0] [7 9 1] [7 10 0] [7 11 1] [7 12 0] [7 13 1] [7 14 0] [7 15 1] [7 16 0] [7 17 1] [7 18 0] [7 19 1] [7 20 1] [7 21 1] [7 22 1] [7 23 1] [7 24 1] [7 25 1])

(defn apply-x [rule]
  (doseq [r rule
          :let [c (first r) coor (rest r)]]
    (if (= 1 c) (apply set-black coor))))

;; (map first (indexed-full-rules "x"))
;; (2 6 16)
;; app.renderer> (map second (indexed-full-rules "x"))
;; ((:s :i :s :s :s :i :s :i :s :s :s :i :s :i :s :s :s :i :s :i :s :s :s :i :s) (:s :s :s :s :s :s :s :i :s :i :s :i :s :i :s :i :s :i :s :s :s :s :s :s :s) (:s :i :s :s :s :i :s :s :s :i :s :s :i :s :i :s :s :s :s :s :s :s :s :i :s))

(defn apply-rule-x [irule grid]
  (let [[index rule] irule]
    (map #(assoc %1 index (rule index)) grid rule)))

(defn apply-rule-x1 [grid irule]
  (let [[index rule] irule]
    (map #(assoc %1 (dec index) %2) grid rule)))

(defn apply-rule-y1 [grid irule]
  (let [[index rule] irule]
    (assoc grid (dec index) rule)))

;; (let [full-rules-x (filter #(full-rule? (second %)) (:x (:rules @app-state)))
;;                     full-irules-x (mapv #(vector (first %) (expand-rule (second %))) full-rules-x)
;;                     g (:grid @app-state)]
;;                 (apply-rule-x (second (first full-irules-x)) g))

;; WORKING
;; (let [full-rules-x (filter #(full-rule? (second %)) (:x (:rules @app-state)))
;;                     full-irules-x (mapv #(vector (first %) (expand-rule (second %))) full-rules-x)
;;                     g (:grid @app-state)]
;;   (reduce apply-rule-x1 g full-irules-x)) 
;; (def t2 (let [full-rules-x (filter #(full-rule? (second %)) (:x (:rules @app-state)))
;;               full-irules-x (mapv #(vector (first %) (expand-rule (second %))) full-rules-x)
;;               g (:grid @app-state)]
;;           (into [] (reduce apply-rule-x1 g full-irules-x))))

;; (def t1 (let [full-rules-x (filter #(full-rule? (second %)) (:x (:rules @app-state)))
;;               full-irules-x (mapv #(vector (first %) (expand-rule (second %))) full-rules-x)
;;               g (:grid @app-state)]
;;           (map #(apply-rule-x % g) full-irules-x)))

;; app.renderer> (def xf (comp (filter odd?) (map inc)))
;; #'app.renderer/xf
;; app.renderer> (transduce xf + 100 (range 5))
;; 106
;; app.renderer> (transduce xf + 100 (range 5))
;; 106
;; (into [] xf (range 5))
;; [2 4]
;; (def filter-full-rules-xf (comp filter full-rule)
;; (def expand-full-rules-xf (comp map expand-rule))
;; (def f-e-irules (comp filter-full-rules-xf expand-full-rules-xf))

;; transduce will immediately (not lazily) reduce over coll with the
;; transducer xform applied to the reducing function f, using init as
;; the initial value if supplied or (f) otherwise. f supplies the
;; knowledge of how to accumulate the result, which occurs in
;; the (potentially stateful) context of the reduce.

;;;; Definitions
  ;; Looking at the `reduce` docstring, we can define a 'reducing-fn' as:
;;  (fn reducing-fn ([]) ([accumulation next-input])) -> new-accumulation
  ;; (The `[]` arity is actually optional; it's only used when calling
;; `reduce` w/o an init-accumulator).

;; We choose to define a 'transducing-fn' as:
;;(fn transducing-fn [reducing-fn]) -> new-reducing-fn

;; * Conceptual:
  ;;   * Elegantly & flexibly unifies concepts like:
  ;;     * Reducing   - (ordered-seq    -> accumulated-val).
  ;;     * Mapping    - (ordered-seq    -> new-ordered-seq).
;;     * 'Reducers' - (unordered-coll -> accumulated-val)

;; (transeduce f-e-irules apply-rule-x g full-irules-x)
;; app.renderer> (def t1 (let [full-rules-x (filter #(full-rule? (second %)) (:x (:rules @app-state)))
;;                     full-irules-x (mapv #(vector (first %) (expand-rule (second %))) full-rules-x)
;;                     g (:grid @app-state)]
;;                 (partial apply-rule-x)
;;                 (transduce )

  ;; I omitted a detail which Rich helped clarify.
  ;; `transduce` _actually_ expects a reducing-fn modified to also accept a new
  ;; `[accumumulation]` arity:
  ;; (fn transducer-ready-reducing-fn
  ;;   ([]) ; Recall that this arity is optional (only needed when no init-accumulator given)
  ;;   ([accumulation]) ; <- This is the new arity to support transducing
  ;;   ([accumulation next-input])
  ;;   )

;; "f should be a function of 2 arguments. If val is not supplied,
;; returns the result of applying f to the first 2 items in coll, then
;; applying f to that result and the 3rd item, etc.

;; 1: apply-rule grid rule1 -> grid1
;; 2: apply-rule grid1 rule2 -> grid2
;; ...

;; :empty
;; :possible
;; :solid - set initially, 100% there
;; :impossible
;; :firm - range gap lowered to length, :p -> :f

;; gr   ru
;; :e & :p == :p
;; :e & :i == :i
;; :e & :s == :s
;; :p & :i == :i
;; :p & :p == :p
;; :p & :s == :s
;; :s & :i == ERROR
;; (map #(if (or (not= %1 :s) (not= %2 :i)) %2) [:e :d] [:s :e])
;; (first (map second (indexed-full-rules "x")))
;; (:s :i :s :s :s :i :s :i :s :s :s :i :s :i :s :s :s :i :s :i :s :s :s :i :s)
;; ((assoc-in (:grid @app-state) [0 2] :s) 0)
;; [:e :e :s :e :e :e :e :e :e :e :e :e :e :e :e :e :e :e :e :e :e :e :e :e :e]

(defn init []
  (js/console.log "Starting Application")

  (js/console.log "Drawing grid")
  (.setDimensions canvas #js {:height (.-innerHeight js/window) :width (.-innerWidth js/window)})
  (set! (.-renderOnAddRemove canvas) false)
  (doseq [x (range 26)]
  (let [line-x (+ grid-x (* 20 x))]
    (.add canvas (f.Line. #js [line-x grid-y line-x (+ grid-y 500)] #js {:stroke "grey"}))))
  (doseq [y (range 26)]
    (let [line-y (+ grid-y (* 20 y))]
      (.add canvas (f.Line. #js [grid-x line-y (+ grid-x 500) line-y] #js {:stroke "grey"}))))
  
  (doseq [rect rect-matrix] (.add canvas rect))
  (set! (.-renderOnAddRemove canvas) true)
  (.renderAll canvas)
  
  (js/console.log "Applying solids grid")
  (def grid-vs1 (apply-solids-v1))
  (swap! app-state assoc :grid (apply-solids-v1))
  (draw-grid-v1 (@app-state :grid))

  (js/console.log "Validating rules")
  (swap! app-state assoc :raw-rules rules)
  (validate-rules :x)
  (validate-rules :y)

  (swap! app-state assoc-in [:rules :x] (index-rule-2 "x"))
  (swap! app-state assoc-in [:rules :y] (index-rule-2 "y"))

  (def t2 (let [full-rules-x (filter #(full-rule? (second %)) (:x (:rules @app-state)))
                full-irules-x (mapv #(vector (first %) (expand-rule (second %))) full-rules-x)
                g (:grid @app-state)]
            (into [] (reduce apply-rule-x1 g full-irules-x))))
  (swap! app-state assoc :grid t2)

  (def t3 (let [full-rules-y (filter #(full-rule? (second %)) (:y (:rules @app-state)))
                full-irules-y (mapv #(vector (first %) (expand-rule (second %))) full-rules-y)
                g (:grid @app-state)]
            (into [] (reduce apply-rule-y1 g full-irules-y))))
  ;; TODO remove applied rules from app-state!
  (swap! app-state assoc :grid t3)
  
  (draw-grid-v1 (@app-state :grid))
  
  )


;; * MISC
;; (doseq [x (range 26) y (range 26)] (prn x y))

;; (doseq [x1 (range 6) y1 (range 6) :let [v ((grid-v2 y1) x1) x (inc x1) y (inc y1)]]
;;   (condp = v
;;     :e (set-white x y)
;;     :s (set-black x y)))

;; ((assoc test-m [3 3] :s) [3 3])
;; :s


;; rule check alg
;; (map first (row 4))
;; (4 5 13 14 22)

