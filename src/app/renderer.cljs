(ns app.renderer
  (:require [cljsjs.fabric]))

(declare initial-matrix)

(defn init []
  (js/console.log "Starting Application"))

;; * FABRIC
(def grid-x (- (/ (.-innerWidth js/window) 2) 250))
(def grid-y (- (/ (.-innerHeight js/window) 2) 290))

(def f js/fabric)
(def canvas (f.Canvas. "canvas-id"))
(.setDimensions canvas #js {:height (.-innerHeight js/window) :width (.-innerWidth js/window)})

;; * GRID
(doseq [x (range 26)]
  (let [line-x (+ grid-x (* 20 x))]
    (.add canvas (f.Line. #js [line-x grid-y line-x (+ grid-y 500)] #js {:stroke "grey"}))))
(doseq [y (range 26)]
  (let [line-y (+ grid-y (* 20 y))]
    (.add canvas (f.Line. #js [grid-x line-y (+ grid-x 500) line-y] #js {:stroke "grey"}))))

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

(defn set-white [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "white")
  (.renderAll canvas))

(defn set-black [x y]
  (.setColor (nth rect-matrix (+ (* (dec y) 25) (dec x))) "black")
  (.renderAll canvas))

(def rect-matrix
  (for [y (range 1 26) x (range 1 26)]
    (make-rect x y)))
(doseq [rect rect-matrix] (.add canvas rect))
(doseq [rect (map #(apply set-black %) initial-matrix)])


(defn row-m [n m] (filter #(= n (second %)) m))
(defn row [n] (filter #(= n (second %)) initial-matrix))
(defn column-m [n m] (filter #(= n (first %)) m))
(defn column [n] (filter #(= n (first %)) initial-matrix))


;; * GCHQ ALGS
(def initial-matrix [[ 4 4] [ 5  4] [13  4] [14  4] [22  4]
                     [ 7 9] [ 8  9] [11  9] [15  9] [16  9] [19  9]
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

;; Verify that all rules have maximum potential 25 spaces
(if (not-empty (filter #(> % 25)
                 (for [rule (:x rules)]
                   (reduce + (interpose 1 rule)))))
  (js/console.log "rules for x invalid, has mode then 25 chars")
  (js/console.log "rules for x valid"))

(if (not-empty (filter #(> % 25)
                 (for [rule (:y rules)]
                   (reduce + (interpose 1 rule)))))
  (js/console.log "rules for y invalid, has mode then 25 chars")
  (js/console.log "rules for y valid"))


;; rule check alg
;; (map first (row 4))
;; (4 5 13 14 22)
