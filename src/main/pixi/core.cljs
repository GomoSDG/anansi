(ns pixi.core
  (:require ["pixi.js" :refer [Application]]
            [promesa.core :as p]))


(defn initialise-pixi [container width height initialiser & {:keys [] :as options}]
  (p/let [app (Application.)
          _ (.init app #js {"width" width "height" height})]
    (.appendChild container (.-canvas app))
    (initialiser app (into {:width width :height height} options))
    app))

