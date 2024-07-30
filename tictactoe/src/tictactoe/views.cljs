(ns tictactoe.views
  (:require [pixi.core :as pixi]
            ["pixi.js" :refer [GraphicsContext]]
            [promesa.core :as p]))

(comment)


(defn initialise-game [app options]
  ())

(defn main []
  (let [container (.getElementById js/document "app")
        app (pixi/initialise-pixi container 640 360 #(println (count %&) %&))]
    (println (GraphicsContext.))))
