(ns minesweeper.core
  (:require [anansi.core :as anansi]
            [promesa.core :as p]))


(def app (atom nil))


(comment
  (let [app (js/PIXI.Application.)]
    (-> (.init app #js {"width" 500 "height" 500})
        (.then (fn []
                 (.appendChild js/document.body (.-canvas app)))))))


(defn initialise-pixi [container width height initialiser & {:keys [] :as options}]
  (let [app (js/PIXI.Application.)]
    (-> (.init app #js {"width" width "height" height})
        (.then (fn []
                 (.appendChild container (.-canvas app))
                 (initialiser app (into {:width width :height height} options)))))
    app))


(defn get-factor-larger-than [n x & [max]]
  (loop [num x]
    (cond
      (= 0 (mod n num))
      num

      (> num (or max ##Inf))
      nil

      :else
      (recur (inc num)))))


(defn block [& [content]]
  {:content (or content :empty)
   :flagged? false
   :revealed? false})

(defn flagged? [{:keys [flagged?]}]
  flagged?)


(defn revealed? [{:keys [revealed?]}]
  revealed?)


(defn has-mine? [{:keys [content] :as data}] 
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
        (reveal! (get sprites coords) (update block :edges #(map (fn [{:keys [cell-id]}] (anansi/get-cell @grid cell-cid)) %)) grid textures)
        (loop [edge-queue edges
               visited #{}
               actions []
               included #{}]
          (let [e (peek edge-queue)
                popped (pop edge-queue)
                edge-cells (anansi/get-edge-cells @grid (:id e))]
            (println "TRAVERSE!")
            (if-not e
              actions
              (recur (if (and (is-f-cell? (complement has-mine?) e)
                              (not (is-f-cell? (complement is-empty?) e))
                              (not (visited (:id e))))
                       (into popped (filter #(and (-> (:id %) (visited) not)
                                                  (not= (:id %) (:id e))
                                                  (not (apply included [(:id %)]))
                                                  (is-f-cell? (complement revealed?) %))
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


(defn create-texture-map []
  (p/let [hidden (.load js/PIXI.Assets "/assets/app/img/minesweeper/unknown_2_128x128.png")
          question (.load js/PIXI.Assets "/assets/app/img/minesweeper/question_2_128x128.png")
          empty (.load js/PIXI.Assets "/assets/app/img/minesweeper/empty_128x128.png")
          eight (.load js/PIXI.Assets "/assets/app/img/minesweeper/8_128x128.png")
          seven (.load js/PIXI.Assets "/assets/app/img/minesweeper/7_128x128.png")
          six (.load js/PIXI.Assets "/assets/app/img/minesweeper/6_128x128.png")
          five (.load js/PIXI.Assets "/assets/app/img/minesweeper/5_128x128.png")
          four (.load js/PIXI.Assets "/assets/app/img/minesweeper/4_128x128.png")
          three (.load js/PIXI.Assets "/assets/app/img/minesweeper/3_128x128.png")
          two (.load js/PIXI.Assets "/assets/app/img/minesweeper/2_128x128.png")
          one   (.load js/PIXI.Assets "/assets/app/img/minesweeper/1_128x128.png")
          zero (.load js/PIXI.Assets "/assets/app/img/minesweeper/empty_128x128.png")]
    {:hidden hidden
     :empty empty
     :question question
     1 one
     2 two
     3 three
     4 four
     5 five
     6 six
     7 seven
     8 eight
     0 zero}))


(defn initialise-minesweeper [app {:keys [width height grid-size]}]
  (p/let [grid (atom (-> (anansi/initialise-grid grid-size grid-size
                                                 :initial-data (block))
                         (place-mines 75)))
          block-width (get-factor-larger-than width 25)
          block-height (get-factor-larger-than height 18)
          queue (atom #queue [])
          sprites (atom {})
          cell-info (anansi/get-cell-info @grid)
          textures (create-texture-map)]
    (doseq [idx (range (count cell-info))]
      (let [{:keys [x y id]} (nth cell-info idx 0)
            block (js/PIXI.Sprite. (textures :hidden))
            xc (* x block-width)
            yc (* y block-height)]
        (set! (.-x block) xc)
        (set! (.-y block) yc)
        (set! (.-height block) block-height)
        (set! (.-width block) block-width)
        (set! (.-eventMode block) "static")
        (.on block "click" #(swap! queue conj {:type :open :coords id}))
        (swap! sprites assoc id block)
        (.addChild (.-stage app) block)
        (.add (.-ticker app)
              (fn [] (when-let [action (peek @queue)]
                       (do
                         (swap! queue into
                                (apply-action {:action action
                                               :action-type (:type action)
                                               :grid grid
                                               :sprites @sprites
                                               :textures textures}))
                         (swap! queue pop)))))))))


(defn main []
  (let [container (.getElementById js/document "app")]
    (println "Hello World!!" container)
    (reset! app (initialise-pixi container 640 360 initialise-minesweeper
                                 :grid-size 20))))


(comment
  (initialise-pixi 20 20))





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


(defn token-won?
  ([grid orientation token x y]
   (token-won? grid orientation token (str x "," y)))
  ([grid orientation token coords]
   (loop [q #queue [(anansi/get-cell grid coords)]
          l 0
          t (set coords)]
     (let [{:keys [neighbours id]} (peek q)
           n' (->> (filter #(and (= (:orientation %) orientation)
                                 (not (t (:cell-id %))))
                           neighbours)
                   (map #(anansi/get-cell grid (:cell-id %)))
                   (filter #(= token (:data %))))]
       (if-not (not-empty n')
         (>= l 2)
         (recur (into (pop q) n')
                (inc l)
                (conj t id)))))))


(comment
  (-> (anansi/initialise-grid 10 10)
      (anansi/set-cell-data 1 0 :X)
      (anansi/set-cell-data 1 1 :X)
      (anansi/set-cell-data 1 2 :X)
      #_(anansi/print-grid)
      (anansi/get-cell-info)
      #_(token-won? :vertical :X 1 0))

  (-> (anansi/initialise-grid 10 10
                       :initial-data (block))
      (place-mines 5)
      (anansi/print-grid))


  (anansi/grid-layout 3 3)

  (anansi/print-grid (anansi/initialise-grid 3 3))

  (comment
    [[[0 0] [1 0] [2 0]]
     [[0 1]]])

  (-> (anansi/initialise-grid 3 3)
      (anansi/set-cell-data  0 0 :X)
      (anansi/get-cell-data 0 0)))
