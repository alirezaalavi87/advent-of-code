(ns dev.user
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as viewer]))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Clerk dev commands
;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (clerk/serve! {:port 7777})

  (clerk/serve! {:watch-paths ["src"] :port 7777})

  ;; either call `clerk/show!` explicitly to show a given notebook, or use the File Watcher described below.
  (clerk/show! "src/aoc/2025/day1/main.clj")

  ;; Show the 🚰 Tap Inspector to inspect values using tap>
  (nextjournal.clerk/show! 'nextjournal.clerk.tap)

  (clerk/build! {:paths ["src/aoc/**.md"
                         "src/aoc/**.clj"]
                 ; :compile-css true
                 ; :ssr true
                 })

  (clerk/clear-cache!))

;;;;;;;;;;;;;;;;;;;;;;
;; Clerk Configuration
;;;;;;;;;;;;;;;;;;;;;;
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

