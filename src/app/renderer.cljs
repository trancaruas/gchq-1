(ns app.renderer
  (:require [cljsjs.fabric]
            ;; [clojure.set :as s]
            ;; [fipp.edn :as pp :refer (pprint)]
            ))

;; https://github.com/clojure-emacs/clj-refactor.el/wiki

;; “C-M-h”
;;      Put region around current defun (“mark-defun”).

;;(fipp [1 2 3])

; (def log js/console.log)

;; boot.user> (clojure.string/index-of "Clojure is fun" "fun")
;; 11
;; boot.user> (clojure.string/last-index-of "Clojure is fun. Sometimes fun" "fun")
;; 26
;; boot.user> (clojure.string/starts-with? "Clojure is fun. Sometimes fun" "fun")
;; false
;; boot.user> (clojure.string/starts-with? "Clojure is fun. Sometimes fun" "Clojure")
;; true
;; boot.user> (clojure.string/includes? "Clojure is fun. Sometimes fun" "is")
;; true
;; boot.user> (clojure.string/ends-with? "Clojure is fun. Sometimes fun" "fun")
;; true


;; * ELECTRON & FABRIC INIT
(def remote (js/require "remote"))
(def browser (remote.require "browser-window"))
(def window (first (browser.getAllWindows)))

(def grid-x (- (/ (.-innerWidth js/window) 2) 50))
(def grid-y (- (/ (.-innerHeight js/window) 2) 200))

(def f js/fabric)
(def canvas (f.Canvas. "canvas-id" #js {:renderOnAddRemove false}))

;; * STATE & RULES
;; vTEMPv
(def s1 #{{:x 1 :y 1 :v :s} {:x 2 :y 1 :v :s}
          {:x 1 :y 2 :v :e} {:x 2 :y 2 :v :s}})

(def grid-set (into #{} (for [x (range 25) y (range 25)]
                          {:x x :y y :v :s})))

;; (defn get-row-set [s row] (map :v (s/select (fn [cell] (= (:x cell) row)) s)))
;; (defn get-col-set [s col] (map :v (s/select (fn [cell] (= (:y cell) col)) s)))
;; (defn get-cell-set [s row col] (first (map :v (s/select (fn [cell] (and (= (:x cell) row) (= (:y cell) col))) s))))

;; Magnitudes slower then vectors
;; (simple-benchmark [] (get-cell-set grid-set 5 7) 1000)
;; WARNING: Use of undeclared Var app.renderer/get-cell-set at line 1 <cljs repl>
;; [], (get-cell-set grid-set 5 7), 1000 runs, 2172 msecs

(def s2 {{[1 1] :s} {[2 1] :s}
         {[1 2] :e} {[2 2] :s}})
;; ^TEMP^

(def app-state (atom {}))

(def solids-v [[4  4] [ 5  4] [13  4] [14  4] [22  4]
               [7  9] [ 8  9] [11  9] [15  9] [16  9] [19  9]
               [7 17] [12 17] [17 17] [21 17]
               [4 22] [ 5 22] [10 22] [11 22] [16 22] [21 22] [22 22]])

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


;; * GRAPHICS PRIMIVITES
(declare solids)
(declare make-rect)
(declare set-white)
(declare set-black)

(defn make-rect [x y]
  (f.Rect. #js {:width 19 :height 19 :fill "white"
                :hasControls   false
                :selectable    false
                :lockMovementX true
                :lockMovementY true
                :left          (+ grid-x (* (dec x) 20) 1)
                :top           (+ grid-y (* (dec y) 20) 1)}))

(defn make-text [x y text & [angle]]
  (let [origin (if (or angle) "left" "right")]
    (f.Text. text #js {:left x :top y :fontSize 15 :originX origin
                       :hasControls   false
                       :selectable    false
                       :lockMovementX true
                       :lockMovementY true
                       :angle (or angle 0)})))

(def rect-matrix
  (for [y (range 1 26) x (range 1 26)]
    (make-rect x y)))

(defn set-white-1 [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "white"))

(defn set-black-1 [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "black"))

(defn set-red [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "#red"))

(defn set-pink [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "#ffd8d8"))
;; #ffb4c7
;; #ffd8d8

;;(doseq [rect (map #(apply set-black %) solids)])

(defn prepare-canvas []
  (.setDimensions canvas #js {:height (.-innerHeight js/window) :width (.-innerWidth js/window)})

  (js/console.log "Drawing grid")
  (set! (.-renderOnAddRemove canvas) false)

  ;; draw grid of lines
  (doseq [i (range 26)]
    (let [line-x (+ grid-x (* 20 i))
          line-y (+ grid-y (* 20 i))]
      (.add canvas (f.Line. #js [line-x grid-y line-x (+ grid-y 500)] #js {:stroke "grey"
                                                                           :hasControls   false
                                                                           :selectable    false
                                                                           :lockMovementX true
                                                                           :lockMovementY true}))
      (.add canvas (f.Line. #js [grid-x line-y (+ grid-x 500) line-y] #js {:stroke "grey"
                                                                           :hasControls   false
                                                                           :selectable    false
                                                                           :lockMovementX true
                                                                           :lockMovementY true}))))

  ;; draw all rects
  (doseq [rect rect-matrix] (.add canvas rect))
  (set! (.-rendexrOnAddRemove canvas) true)
  (.renderAll canvas))

;; * GRID FUNCTIONS
(def grid-v1 (into [] (repeatedly 25 #(into [] (replicate 25 :e)))))
;; (simple-benchmark [] (get (get grid-v1 3) 3) 1000000)
;; [], (get (get grid-v1 3) 3), 1000000 runs, 283 msecs

(defn draw-grid-m [grid]
  (doseq [e grid]
    (condp = (second e)
      :e (apply set-white-1 (first e))
      :s (apply set-black-1 (first e))
      (js/console.log (str "error value to draw in " (first e)))))
  (.renderAll canvas))

;; (assoc-in grid-v1 [0 0] :s)
(defn get-row [n g]
  (get g n))
;; (simple-benchmark [] (get-row-v1 4 grid-vs1) 10000)
;; [], (get-row-v1 4 grid-vs1), 10000 runs, 3 msecs

(defn get-col [n g]
  (map #(get % n) g))
;; (simple-benchmark [] (get-col-v1 4 grid-vs1) 10000)
;; [], (get-col-v1 4 grid-vs1), 10000 runs, 10 msecs

;; TODO reduce жы!!!
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

(defn applied-rules [axis]
  (keep-indexed (fn [i e] (if (= :a (first e)) i )) (axis (:rules @app-state))))
;; (applied-rules :x)
;; (2 6 16)

(declare strike-rule-h)
(declare strike-rule-v)
(defn draw-grid-v1 [& [g]]
  (let [grid (or g (@app-state :grid))]
    (doseq [x1 (range 25) y1 (range 25)
            :let [v ((grid y1) x1) x (inc x1) y (inc y1)]]
      (condp = v
        :e (set-white-1 x y)
        :s (set-black-1 x y)
        :E (set-red x y)
        :i (set-pink x y))))
  (doseq [i (applied-rules :x)]
    (strike-rule-h i))
  (doseq [i (applied-rules :y)]
    (strike-rule-v i))
  (.renderAll canvas))

;; * RULES FUNCTIONS
;; Verify that all rules have maximum potential 25 spaces
(defn validate-rules [k]
  (if (empty (filter #(> % 25)
                   (for [rule (k rules)]
                     (reduce + (interpose 1 rule)))))
    (do (js/console.log (str "rules for " (name k) " valid"))
        true)
    (js/console.log (str "rules for " (name k) " invalid, has more then 25 chars"))))

;; (find-rules-length (max-rule-length :x) :x)
;; [[3 [:u [1 3 1 1 5 1 3 1]]] [8 [:u [2 1 2 1 8 2 1]]] [21 [:u [1 3 1 1 1 2 1 1 4]]]]
;; [1 3 1 1 5 1 3 1]
;; (get-row 3 (:grid @app-state))
;; [:e :e :e :s :s :e :s :e :e :e :e :e :s :s :e :e :e :e :e :e :e :s :e :e :e]
;; (expand-rule [1 3 1 1 5 1 3 1]))
;; [:s :i :s :s :s :i :s :i :s :i :s :s :s :s :s :i :s :i :s :s :s :i :s]

(defn cell-compare [rule cell]
  (case [rule cell]
    [:s :e] :s
    [:s :s] :s
    [:i :e] :i
    [:i :s] :E
    :U))

;; (def i1 (mapv #(vector % [1 25]) [1 3 1 1 5 1 3 1]))
;; add one element if interpose arg consists of only one

(defn interpose-e [sep col]
  (if (empty? col) nil
      (if (= 1 (count col))
        (conj col 1)
        (conj (interpose sep col) 1))))

(defn upper-limit [e col]
  (let [[r [l h]] e]
    [r [l (- 25 (reduce + (interpose-e 1 (map first col))))]]))

(defn u-limits [col]
  (if (empty? col) nil
      (conj (u-limits (rest col)) (upper-limit (first col) (rest col)) )))
;; (u-limits i1)
;; ([1 [1 3]] [3 [1 7]] [1 [1 9]] [1 [1 11]] [5 [1 17]] [1 [1 19]] [3 [1 23]] [1 [1 25]])

(defn lower-limit [e col]
  (let [[r [l h]] e]
    [r [(+ 1 (reduce + (interpose-e 1 (reverse (map first col))))) h]]))

(defn l-limits [col]
  (if (empty? col) nil
      (conj (l-limits (butlast col)) (lower-limit (last col) (butlast col)))))
;; (reverse (l-limits i1))
;; ([1 [1 25]] [3 [3 25]] [1 [6 25]] [1 [8 25]] [5 [10 25]] [1 [16 25]] [3 [18 25]] [1 [22 25]])


;;   (-> i1
;;     u-limits
;;     l-limits
;;     reverse)
;; ([1 [1 3]] [3 [3 7]] [1 [7 9]] [1 [9 11]] [5 [11 17]] [1 [17 19]] [3 [19 23]] [1 [23 25]])
;; [:e :e :e :s :s :e :s :e :e :e :e :e :s :s :e :e :e :e :e :e :e :s :e :e :e]

(def i2 (mapv #(vector % [1 25]) [2 1 2 1 8 2 1]))
;; (-> i2 u-limits l-limits reverse)
;; ([2 [1 4]] [1 [4 6]] [2 [6 9]] [1 [9 11]] [8 [11 20]] [2 [20 23]] [1 [23 25]])
(def r2 (get-row 8 (:grid @app-state)))
;; [:e :e :e :e :e :e :s :s :e :e :s :e :e :e :s :s :e :e :s :e :e :s :e :e :e]

(defn group [coll] (partition-by identity coll))
;; app.renderer> (group r2)
;; ((:e :e :e :e :e :e) (:s :s) (:e :e) (:s) (:e :e :e) (:s :s) (:e :e) (:s) (:e :e) (:s) (:e :e :e))
;; app.renderer> (for [el (group r2)]
;;                 [(first el) (count el)])
;; ([:e 6] [:s 2] [:e 2] [:s 1] [:e 3] [:s 2] [:e 2] [:s 1] [:e 2] [:s 1] [:e 3])


;;                        v existing fully in range of rule
;;                                       v existing fully in range of rule
;;                                                                            v existing fully in range of rule
;;  2[1 4]] 1[4 6]]    2[6 9]        1[9 11]]      8[11 20]          ]     2[20 23]     1[23 25])
;;                     2[7 8]        1[11 11]      2[15 16]  1[19 19]      1[22 22]
;;                        v      reducing range, range is now equal to length
;;                                       v      reducing range, range is now equal to length
;;                   v        v   v            v setting impossible cells 
;;                  i6 2[7f8] i9 i10 1[11f11] i12
;;                                                   v recalculating range, range is equal to length, fixing
;;                                                                      v setting impossible cells
;;          1[4 5]                                 8[13    f   20    ] i21
;;                                                                             | recalc range, equal to len
;;                                                                             v     v setting impossible  
;;                                                                         2[22f23] i24
;;                                                                                          v recalc-range-fix
;;                                                                                      1[25f25]

;; make mask of impossible cells including fixed to match remaining rules
;; save results of iteration into @app-state and compare to next result
;; no need to save once rule is exhausted - the next time we re-run
;; this rule, grid most probably will change and mask recompute will be required

;; mask v1 for solved rule entry 2[7f8] -> 4[6 9]
;; mask v2 (:s :s :s :s :s :m :m :m :m :s :s :s :s :s :s :s :s :s :s :s :s :s :s :s :s)

(defn expand-rule [rule]
  (->> rule
       (interpose :i)
       (map #(if (> % 0) (map (constantly :s) (range %)) %))
       (flatten)
       (into [])))

(defn set-row [state index row]
  (assoc-in state [:grid index] row))

(defn set-col [state index col]
  (assoc state :grid (mapv #(assoc %1 index %2) (:grid state) col)))

;; rules application
(defn apply-rule-x [state irule]
  (let [[index [_ rule]] irule
        grid (:grid state)]
    (set-row state index (mapv #(cell-compare %1 %2)
                               (expand-rule rule)
                               (get-row index grid)))))

(defn apply-rule-y [state irule]
  (let [[index [_ rule]] irule
        grid (:grid state)]
    (set-col state index (mapv #(cell-compare %1 %2)
                               (expand-rule rule)
                               (get-col index grid)))))

(defn apply-rules [rules-x rules-y state]
  (as-> state s
    (reduce apply-rule-x s rules-x)
    (reduce apply-rule-y s rules-y)))

;; rule length functions
(defn rule-length [state-rule]
  (reduce + (interpose 1 (second state-rule))))
;; (rule-length (first (:y (:rules @app-state))))
;; 23

(defn max-rule-length [axis]
  (reduce max (map rule-length (filter #(not= :a (first %)) (axis (:rules @app-state))))))
;; (max-rule-length :x)
;; 25

(defn find-rules-length [l axis]
  (into [] (keep-indexed (fn [i e] (if (= l (rule-length e)) [i e])) (axis (:rules @app-state)))))
;; (find-rules-length 25 :x)
;; [[2 [:u [1 3 1 3 1 3 1 3 1]]] [6 [:u [7 1 1 1 1 1 7]]] [16 [:u [1 3 3 2 1 8 1]]]]

;; marking rules in state as applied
(defn mark-rule-x [state irule]
  (let [[index rule] irule
        arule (vector :a (second rule))]
    (assoc-in state [:rules :x index] arule)))

(defn mark-rule-y [state irule]
  (let [[index rule] irule
        arule (assoc rule 0 :a)]
    (assoc-in state [:rules :y index] arule)))

(defn mark-applied-rules [rules-x rules-y state]
  (as-> state s
    (reduce mark-rule-x s rules-x)
    (reduce mark-rule-y s rules-y)))


;; * MUSINGS
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

;; Conceptual:
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

;; :empty
;; :possible
;; :solid - set initially, 100% there
;; :impossible
;; :firm - range gap lowered to length, :p -> :f

;; * RULES DISPLAY
(defn draw-rule-h [n rule-string]
  (.add canvas (make-text (- grid-x 15) (+ grid-y (* n 20) 2) rule-string)))

(defn draw-rules-h []
  (doseq [i (range 25)]
    (let [rule ((:x rules) i)]
      (draw-rule-h i (apply str (interpose " " rule))))))

(defn draw-rule-v [n rule-string]
  (.add canvas (make-text (+ grid-x (* n 20) 2) (- grid-y 15) rule-string -90)))

(defn draw-rules-v []
  (doseq [i (range 25)]
    (let [rule ((:y rules) i)]
      (draw-rule-v i (apply str (interpose " " rule))))))

(defn strike-rule-h [n]
  (let [len (* 11 (count ((:x rules) n)))]
    (.add canvas
          (f.Line. #js [(- grid-x 15) (+ grid-y (* n 20) 10) (- grid-x 15 len) (+ grid-y (* n 20) 10)]
                   
                   #js {:stroke "red"
                        :hasControls   false
                        :selectable    false
                        :lockMovementX true
                        :lockMovementY true}))))

(defn strike-rule-v [n]
  (let [len (* 11 (count ((:y rules) n)))]
    (.add canvas
          (f.Line. #js [(+ grid-x (* n 20) 10) (- grid-y 15) (+ grid-x (* n 20) 10) (- grid-y 15 len)]
                   #js {:stroke "red"
                        :hasControls   false
                        :selectable    false
                        :lockMovementX true
                        :lockMovementY true}))))


;; * MAIN
(defn init []
  (js/console.log "Starting Application")
  ;; (set! (. js/document -title) "GCHQ")
  (window.setTitle "GCHQ Solver")
  (prepare-canvas)
  (draw-rules-h)
  (draw-rules-v)

  ;; (js/console.log "Validating rules")
  ;; (swap! app-state assoc :raw-rules rules)
  ;; (validate-rules :x)
  ;; (validate-rules :y)

  ;; rule: [1 3 1 3 1 3 1 3 1]
  ;; rule with state: [:a [1 3 1 3 1 3 1 3 1]]
  ;;   where :a - applied, :u - unapplied
  (swap! app-state assoc-in [:rules]
         {:x (mapv #(vector :u %) (:x rules))
          :y (mapv #(vector :u %) (:y rules))})
  
  (js/console.log "Applying initial solids")
  (swap! app-state assoc :grid (apply-solids-v1))

  ;; apply rules to grid and mark them
  (let [max-rules-x (find-rules-length (max-rule-length :x) :x)
        max-rules-y (find-rules-length (max-rule-length :y) :y)]
    (->> @app-state
         (apply-rules max-rules-x max-rules-y)
         (mark-applied-rules max-rules-x max-rules-y)
         (reset! app-state)
         :grid
         (draw-grid-v1)))
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
