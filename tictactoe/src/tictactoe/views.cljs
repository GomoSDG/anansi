(ns tictactoe.views
  (:require [pixi.core :as pixi]
            ["pixi.js" :refer [Graphics Sprite Text]]
            [anansi.core :as anansi]
            [clojure.core.async :refer [chan go-loop <! put!]]
            [tictactoe.core :as tictactoe]))


(def game-grid-height 10)
(def game-grid-width 10)
(def token-win-length 5)



(defn draw-line [app & pairs]
  (let [pairs (partition-all 2 pairs)
        [xs ys] (first pairs)
        g (-> (Graphics.)
              (.lineStyle 2, "white")
              (.moveTo xs ys))]
    (doseq [[x y] (rest pairs)]
      (.lineTo g x y))
    (.stroke g)
    (-> (.-stage app)
        (.addChild g))))


(defn o-graphic [block-size line-size]
  (-> (Graphics.)
      (.lineStyle line-size)
      (.circle 0 0 (/ block-size 4))
      (.stroke)))


(defn x-graphic [block-size line-size]
  (let [half-block (/ block-size 2)]
    (-> (Graphics.)
        (.lineStyle line-size "white")
        (.moveTo 0 0)
        (.lineTo half-block half-block)
        (.stroke)
        (.moveTo half-block 0)
        (.lineTo 0 half-block)
        (.stroke))))


(defn generate-texture [renderer graphic]
  (.generateTexture renderer graphic))


(defn set-sprite-texture [{:keys [sprite x y]} texture box-width box-height]
  (let [box-size (min box-width box-height)]

    (set! (.-texture sprite) texture)
    (set! (.-x sprite) (+ (* x box-width) (/ box-size 4)))
    (set! (.-y sprite) (+ (* y box-height) (/ box-size 4)))
    (set! (.-width sprite) (/ box-size 2))
    (set! (.-height sprite) (/ box-size 2))))


(defn setup-stage [app grid-width grid-height box-width box-height]
  (doseq [x (range box-width (-> (.-canvas app) (.-width)) box-width)]
    (draw-line app x 0 x (* box-height grid-height)))
  (doseq [y (range box-height (-> (.-canvas app) (.-height)) box-height)]
    (draw-line app 0 y (* box-width grid-width) y)))


(defn generate-textures [app block-size line-size]
  {:O (generate-texture (.-renderer app) (o-graphic block-size line-size))
   :X (generate-texture (.-renderer app) (x-graphic block-size line-size))})


(defn prepare-sprites [action-chan box-width box-height info]
  (reduce (fn [acc {:keys [x y id]}]
            (let [sprite (Sprite.)]

              (set! (.-x sprite) (* x box-width))
              (set! (.-y sprite) (* y box-width))
              (set! (.-width sprite) box-width)
              (set! (.-height sprite) box-height)
              (set! (.-eventMode sprite) "static")
              (.on sprite "click" #(put! action-chan {:type :place-token :coords id}))
              (assoc acc id {:sprite sprite
                             :x x
                             :y y})))
          {}
          info))


(defmulti apply-action (fn [{:keys [type]} _] type))


(defmethod apply-action :place-token
  [{:keys [coords]} {:keys [grid tokens textures box-width box-height sprites placed-tokens]}]
  (let [token (first @tokens)
        texture (textures token)]
    (-> (get sprites coords)
        (set-sprite-texture texture box-width box-height))
    (swap! grid anansi/set-cell-data coords token)
    (swap! tokens rest)
    (swap! placed-tokens inc)))


(defn draw-text [{:keys [app]} wording]
  (let [text-style  (clj->js {"stroke" {"width" 5
                                        "join" "round"
                                        "color" "#90EE90"}
                              "fontSize" 50
                              "fill" "#1099bb"})
        text (Text. #js {"text" wording
                         "style" text-style})]
    (set! (.-x text) (- (/ (.-width (.-canvas app)) 2) (/ (.-width text) 2)))
    (set! (.-y text) (- (/ (.-height (.-canvas app)) 2) (/ (.-height text) 2)))
    (-> (.-stage app)
        (.addChild text))))


(defn token-won! [context token]
  (draw-text context (str (name token) " Won!")))


(defn draw! [context]
  (draw-text context "It's a draw"))


(comment
  (anansi/initialise-grid 3 3))


(defn initialise-game [app {:keys [width height]}]
  (let [action-chan (chan 10000)
        grid (atom (anansi/initialise-grid game-grid-width game-grid-height))
        info (anansi/get-cell-info @grid)
        box-width (/ width game-grid-width)
        box-height (/ height game-grid-height)
        block-size (min box-width box-height)
        textures (generate-textures app block-size 5)
        sprites (prepare-sprites action-chan box-width box-height info)
        tokens (atom (cycle [:X :O]))
        size (count info)
        placed-tokens (atom 0)]

    (setup-stage app game-grid-width game-grid-height box-width box-height)

    (doseq [{:keys [sprite]} (vals sprites)]
      (-> (.-stage app)
          (.addChild sprite)))

    ;; Start game loop
    (go-loop []
      (let [{:keys [coords] :as action} (<! action-chan)
            context {:textures textures
                     :sprites sprites
                     :tokens tokens
                     :grid grid
                     :box-width box-width
                     :box-height box-height
                     :app app
                     :placed-tokens placed-tokens}
            token (first @tokens)]
        (apply-action action context)
        (cond
          (tictactoe/token-won? @grid coords token-win-length)
          (token-won! context token)

          (= size @placed-tokens)
          (draw! context)

          :else
          (recur))))))


(defn main []
  (let [container (.getElementById js/document "app")
        app (pixi/initialise-pixi container
                                  (min (- (.-innerWidth js/window) 20) (- (.-innerHeight js/window) 20))
                                  (min (- (.-innerWidth js/window) 20) (- (.-innerHeight js/window) 20))
                                  initialise-game)]))
