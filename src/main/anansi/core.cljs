(ns anansi.core)


(defn calculate-orientation [x1 x2 y1 y2]
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

(defn generate-neighbours [x y width height]
  (let [c (str x "," y)]
    (->> (for [xe (range (dec x) (+ x 2))
               ye (range (dec y) (+ y 2))
               :let [ce  (str xe "," ye)]
               :when (and (not= c ce)
                          (>= xe 0)
                          (>= ye 0)
                          (< xe width)
                          (< ye height))]
           {:cell-id ce
            :orientation (calculate-orientation x xe y ye)})
         (set))))


(defn generate-grid-data [width height initial-data]
  (->> (for [x (range width)
             y (range height)]
         {:x x
          :y y
          :edges (generate-neighbours x y width height)
          :data initial-data
          :id (str x "," y)})
       (map (juxt :id identity))
       (into {})))


(defn initialise-grid [width height & {:keys [initial-data]}]
  {:width width
   :height height
   :data (generate-grid-data width height initial-data)})


(defn grid-layout [width height]
  (->> (for [y (range width)
             x (range height)]
         {:coords (str x "," y)
          :x x
          :y y})
       (partition-all width)))


(defn set-cell-data
  ([grid x y data]
   (set-cell-data grid (str x "," y) data))
  ([grid coords data]
   (update-in grid [:data coords] assoc :data data)))


(defn update-cell-data
  ([grid coords f & data]
   (apply update-in (into [grid [:data coords :data] f] data))))


(defn get-cell
  ([grid x y]
   (get-cell grid (str x "," y)))
  ([grid coords]
   (get-in grid [:data coords])))


(defn get-cell-info [{:keys [data]}]
  (->> (vals data)
       (mapv (fn [{:keys [x y id]}]
               {:x x
                :y y
                :id id}))))


(defn get-cell-data
  ([grid x y]
   (get-cell-data grid (str x "," y)))
  ([grid coords]
   (-> (get-cell grid coords)
       (:data))))


(defn get-cell-edges
  ([grid x y]
   (get-cell-edges grid (str x "," y)))
  ([grid coords]
   (-> (get-cell grid coords)
       (:edges))))


(defn get-edge-cells
  ([grid x y]
   (get-edge-cells grid (str x "," y)))
  ([grid coords]
   (map (fn [{:keys [cell-id]}]
          (get-cell grid cell-id))
        (get-cell-edges grid coords))))


(defn print-grid [{:keys [width height] :as grid}]
  (let [layout (grid-layout width height)]
    (doseq [rows layout]
      (doseq [coords rows]
        (print "  |" (get-cell-data grid coords) "|  "))
      (print "\n"))))
