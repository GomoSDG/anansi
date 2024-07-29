(ns minesweeper.core
  (:require [anansi.core :as anansi]))


(defn block [& [content]]
  {:content (or content :empty)
   :flagged? false
   :revealed? false})


(defn flagged? [{:keys [flagged?]}]
  flagged?)


(defn revealed? [{:keys [revealed?]}]
  revealed?)


(defn has-mine? [{:keys [content]}] 
  (= :mine content))


(defn is-empty? [{:keys [content]}]
  (= :empty content))


(defn is-f-cell? [f cell]
  (-> (:data cell)
      (f)))


(defn reveal! [sprite {:keys [data id edges]} grid textures]
  (let [mine-edges-count (-> (filter #(is-f-cell? has-mine? %) edges)
                             (count))]
    (swap! grid anansi/update-cell-data id assoc :revealed? true)
    (set! (.-texture sprite) (textures (if (> mine-edges-count 0)
                                         mine-edges-count
                                         (:content data))))))


(defmulti apply-action :action-type)


(defmethod apply-action :open
  [{:keys [action grid sprites textures]}]
  (let [{coords :coords} action
        block (anansi/get-cell @grid coords)
        edges (into #queue [] (anansi/get-edge-cells @grid coords))]
    (if-not (is-f-cell? has-mine? block)
      (when (is-f-cell? (complement revealed?) block)
        (reveal! (get sprites coords) (update block :edges #(map (fn [{:keys [cell-id]}] (anansi/get-cell @grid cell-id)) %)) grid textures)
        (loop [edge-queue edges
               visited #{}
               actions []
               included #{}]
          (let [e (peek edge-queue)
                popped (pop edge-queue)
                edge-cells (anansi/get-edge-cells @grid (:id e))]
            (if-not e
              actions
              (recur (if (and (is-f-cell? (complement has-mine?) e)
                              (not (is-f-cell? (complement is-empty?) e))
                              (not (visited (:id e))))
                       (into popped (filter #(and (-> (:id %) (visited) not)
                                                  (not= (:id %) (:id e))
                                                  (not (apply included [(:id %)]))
                                                  (is-f-cell? (complement revealed?) %)
                                                  (is-f-cell? (complement has-mine?) e))
                                            edge-cells))
                       popped)
                     (conj visited (:id e))
                     (if (is-f-cell? has-mine? e)
                       actions
                       (conj actions {:type :reveal :coords (:id e)}))
                     (into included (map :id edge-cells)))))))
      [{:type :game-lost}])))


(defmethod apply-action :game-lost
  [_]
  (println "LOST!"))


(defmethod apply-action :reveal
  [{:keys [action grid sprites textures]}]
  (let [{coords :coords} action
        sprite (get sprites coords)]
    (reveal! sprite (update (anansi/get-cell @grid coords) :edges #(map (fn [{:keys [cell-id]}]  (anansi/get-cell @grid cell-id)) %)) grid textures)
    []))


(defn generate-mine-coords [grid-size num-mines]
  (loop [mine-coords #{}]
    (let [coords (str (rand-int grid-size) "," (rand-int grid-size))]
      (if (= (count mine-coords) num-mines)
        mine-coords
        (recur (if (contains? mine-coords coords)
                 mine-coords
                 (conj mine-coords coords)))))))


(defn place-mines [{:keys [height] :as grid} num-mines]
  (let [mine-coords (generate-mine-coords height num-mines)
        mine (block :mine)]
    (reduce (fn [grid coords]
              (anansi/set-cell-data grid coords mine))
            grid
            mine-coords)))


(comment
  (-> (anansi/initialise-grid 3 3)
      (anansi/set-cell-data 1 0 :X)
      (anansi/set-cell-data 1 1 :X)
      (anansi/set-cell-data 1 2 :X)
      #_(anansi/get-cell-data "1,1")
      (anansi/print-grid)
      #_(token-won? :vertical :X 1 0))

  (-> (anansi/initialise-grid 10 10 :initial-data (block))
      (place-mines 5)
      (anansi/print-grid))


  (anansi/grid-layout 3 3)

  (anansi/print-grid (anansi/initialise-grid 3 3))

  (-> (anansi/initialise-grid 3 3)
      (anansi/set-cell-data  0 0 :X)
      (anansi/get-cell-data 0 0)))
