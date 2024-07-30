(ns tictactoe.core
  (:require [anansi.core :as anansi]))


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
  (-> (anansi/initialise-grid 3 3)
      (anansi/set-cell-data 1 0 :X)
      (anansi/set-cell-data 1 1 :X)
      (anansi/set-cell-data 1 2 :X)
      #_(anansi/get-cell-data "1,1")
      (anansi/print-grid)
      #_(token-won? :vertical :X 1 0)))
