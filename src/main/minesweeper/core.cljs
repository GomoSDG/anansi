(ns minesweeper.core)


(defn main []
  (println "Hello World!"))


(defn calculate-label [x1 x2 y1 y2]
  (let [num (- y2 y1)
        denum (- x2 x1)
        slope (/ num denum)]
    (cond
      (= denum 0)
      :vertical

      (> slope 0)
      :right

      (< slope 0)
      :left

      (zero? 0)
      :horizontal)))


(defn initialise-grid [width height]
  (for [x (range width)
        y (range height)

        :let [c (str x "," y)
              edges (->> (for [xe (range (dec x) (+ x 2))
                               ye (range (dec y) (+ y 2))
                               :let [ce  (str xe "," ye)]
                               :when (and (not= c ce) (>= xe 0) (>= ye 0) (< xe width) (< ye height))]
                           {:node-id ce
                            :label (calculate-label x xe y ye)})
                         (set))]]
    (do
      (println "x = " x " : " (range (dec x) (+ x 2)))
      (println "y = " y " : " (range (dec y) (+ y 2)))
      {:x x
       :y y
       :neighbours edges
       :id c})))


(comment
  (initialise-grid 3 3)
  ;; Node
  {:content :empty ; :empty | :mine
   :x 0 ; num
   :y 0 ; num
   :neighbours []
   :id "0,0"}
  ;; Edge
  {:node-id "0,0"}
  )
