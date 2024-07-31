(ns minesweeper.views
  (:require [promesa.core :as p]
            [clojure.core.async :refer [chan go-loop <! put!]]
            [minesweeper.core :as minesweeper]
            [anansi.core :as anansi]
            [pixi.core :as pixi]
            ["pixi.js" :refer [Assets Sprite Application]]))


(def app (atom nil))


(defn get-factor-larger-than [n x & [max]]
  (loop [num x]
    (cond
      (= 0 (mod n num))
      num

      (> num (or max ##Inf))
      nil

      :else
      (recur (inc num)))))


(defn create-texture-map []
  (p/let [hidden (.load Assets "/assets/app/img/minesweeper/unknown_2_128x128.png")
          question (.load Assets "/assets/app/img/minesweeper/question_2_128x128.png")
          empty (.load Assets "/assets/app/img/minesweeper/empty_128x128.png")
          eight (.load Assets "/assets/app/img/minesweeper/8_128x128.png")
          seven (.load Assets "/assets/app/img/minesweeper/7_128x128.png")
          six (.load Assets "/assets/app/img/minesweeper/6_128x128.png")
          five (.load Assets "/assets/app/img/minesweeper/5_128x128.png")
          four (.load Assets "/assets/app/img/minesweeper/4_128x128.png")
          three (.load Assets "/assets/app/img/minesweeper/3_128x128.png")
          two (.load Assets "/assets/app/img/minesweeper/2_128x128.png")
          one   (.load Assets "/assets/app/img/minesweeper/1_128x128.png")
          zero (.load Assets "/assets/app/img/minesweeper/empty_128x128.png")]
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
  (p/let [grid (atom (-> (anansi/initialise-grid grid-size grid-size :initial-data (minesweeper/block))
                         (minesweeper/place-mines 75)))
          block-width (get-factor-larger-than width 25)
          block-height (get-factor-larger-than height 18)
          sprites (atom {})
          cell-info (anansi/get-cell-info @grid)
          textures (create-texture-map)
          action-chan (chan 1000)]
    (doseq [idx (range (count cell-info))]
      (let [{:keys [x y id]} (nth cell-info idx 0)
            block (Sprite. (textures :hidden))
            xc (* x block-width)
            yc (* y block-height)]
        (set! (.-x block) xc)
        (set! (.-y block) yc)
        (set! (.-height block) block-height)
        (set! (.-width block) block-width)
        (set! (.-eventMode block) "static")
        (.on block "click" #(put! action-chan {:type :open :coords id}))
        (swap! sprites assoc id block)
        (.addChild (.-stage app) block)
        (go-loop []
          (let [action (<! action-chan)]
            (doseq [a (minesweeper/apply-action
                       {:action action
                        :action-type (:type action)
                        :grid grid
                        :sprites @sprites
                        :textures textures})]
              (put! action-chan a)))
          (recur))))))


(defn main []
  (let [container (.getElementById js/document "app")]
    (reset! app (pixi/initialise-pixi container 640 360 initialise-minesweeper
                                 :grid-size 20))))

