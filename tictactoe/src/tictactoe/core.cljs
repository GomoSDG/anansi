(ns tictactoe.core
  (:require [anansi.core :as anansi]))


(defn token-won?
  ([grid orientation token x y]
   (token-won? grid orientation token (str x "," y)))
  ([grid orientation token coords]
   (loop [queue #queue [(anansi/get-cell grid coords)]
          length 0
          traversed (set coords)]
     (let [{:keys [neighbours id]} (peek queue)
           ;; Get all the neighbours that are in the required orientation and not traversed
           neighbours (->> (filter #(and (= (:orientation %) orientation)
                                         (not (traversed (:cell-id %))))
                                   neighbours)
                           (map #(anansi/get-cell grid (:cell-id %)))
                           (filter #(= token (:data %))))]
       (if-not (not-empty neighbours)
         (>= length 2) ;; Win if length is meets requirement
         (recur (into (pop queue) neighbours)
                (inc length)
                (conj traversed id)))))))


(comment
  (-> (anansi/initialise-grid 3 3)
      (anansi/set-cell-data 1 0 :X)
      (anansi/set-cell-data 1 1 :X)
      (anansi/set-cell-data 1 2 :X)
      #_(anansi/get-cell-data "1,1")
      (anansi/print-grid)
      #_(token-won? :vertical :X 1 0)))
