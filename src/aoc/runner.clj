(ns aoc.runner
  (:gen-class))

(defn resolve-namespace [year day]
  (let [ns-symbol (symbol (format "aoc.%s.day%s.main" year day))]
    (try
      (require ns-symbol)
      (find-ns ns-symbol)
      (catch Exception e
        (println "Error loading namespace:" (.getMessage e))
        nil))))

(defn -main [& args]
  (when (< (count args) 2)
    (println "Usage: clj -M -m aoc.runner <year> <day> <& args>")
    (System/exit 1))
  (let [[year day & rest] args
        target-ns (resolve-namespace year day)]
    (if-not target-ns
      (do
        (println (format "Namespace for year %s, day %s not found" year day))
        (System/exit 1))
      (let [main-fn (ns-resolve target-ns '-main)]
        (if main-fn
          (apply main-fn rest)
          (println "No main function found in the namespace"))))))

