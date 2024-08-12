(ns tictactoe.core
  (:require [anansi.core :as anansi]))


(defn token-won?
  ([grid x y token-length orientation]
   (token-won? grid (str x "," y) token-length orientation))
  ([grid coords token-length orientation]
   (loop [queue #queue [(anansi/get-cell grid coords)]
          length 1
          traversed (set coords)]
     (let [{:keys [edges data id]} (peek queue)
           ;; Get all the neighbours that are in the required orientation and not traversed
           neighbours (->> (filter #(and (= (:orientation %) orientation)
                                         (not (traversed (:cell-id %))))
                                   edges)
                           (map #(anansi/get-cell grid (:cell-id %)))
                           (filter #(= data (:data %))))]
       (if-not (not-empty neighbours)
         (>= length token-length) ;; Win if length is meets requirement
         (recur (into (pop queue) neighbours)
                (inc length)
                (conj traversed id))))))
  ([grid coords token-length]
   (true? (some (partial token-won? grid coords token-length) [:vertical :left :right :horizontal])))
  ([grid coords]
   (token-won? grid coords 3)))


(comment
  (-> (anansi/initialise-grid 3 3)
      (anansi/set-cell-data 1 0)
      (anansi/set-cell-data 1 1)
      (anansi/set-cell-data 1 2)
      #_(anansi/get-cell-data "1,1")
      (anansi/print-grid)
      #_(token-won? :vertical 1 0)))
