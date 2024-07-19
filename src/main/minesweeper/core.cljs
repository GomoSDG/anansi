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
      :left

      (< slope 0)
      :right

      (zero? 0)
      :horizontal)))


(defn initialise-grid [width height]
  {:width width
   :height height
   :data (->> (for [x (range width)
                    y (range height)

                    :let [c (str x "," y)
                          edges (->> (for [xe (range (dec x) (+ x 2))
                                           ye (range (dec y) (+ y 2))
                                           :let [ce  (str xe "," ye)]
                                           :when (and (not= c ce) (>= xe 0) (>= ye 0) (< xe width) (< ye height))]
                                       {:cell-id ce
                                        :label (calculate-label x xe y ye)})
                                     (set))]]
                {:x x
                 :y y
                 :neighbours edges
                 :content nil
                 :id c})
              (map (juxt :id identity))
              (into {}))})


(defn set-cell-content
  ([grid x y content]
   (set-cell-content grid (str x "," y) content))
  ([grid coords content]
   (update-in grid [:data coords] assoc :content content)))


(defn get-cell
  ([grid x y]
   (get-cell grid (str x "," y)))
  ([grid coords]
   (get-in grid [:data coords])))


(defn get-cell-content
  ([grid x y]
   (get-cell-content grid (str x "," y)))
  ([grid coords]
   (-> (get-cell grid coords)
       (:content))))


(comment
  (-> (initialise-grid 3 3)
      (set-cell-content  0 0 :X)
      (get-cell 0 0))

  )


(comment
  (time (initialise-grid 2 2))
  ;; Node
  {:content :empty ; :empty | :mine
   :x 0 ; num
   :y 0 ; num
   :neighbours []
   :id "0,0"}
  ;; Edge
  {:node-id "0,0"}
  )
