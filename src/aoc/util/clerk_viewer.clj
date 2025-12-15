(ns aoc.util.clerk-viewer
  (:require
   [nextjournal.clerk :as clerk]
   [nextjournal.clerk.viewer :as viewer]))

(defn defn? [cell]
  (some-> cell :result :nextjournal/value ::clerk/var-from-def deref fn?))

(def custom-cell-viewer
  (update viewer/cell-viewer
          :transform-fn comp
          (clerk/update-val (fn [cell]
                              (update-in cell [:settings ::clerk/visibility]
                                         #(if
                                              (defn? cell) {:result :hide}
                                              %))))))

(clerk/add-viewers! [custom-cell-viewer])
