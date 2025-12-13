(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'aoc/puzzles)
(def class-dir "target/classes")
(def version (format "0.%s.0" (b/git-count-revs nil)))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file
          :manifest {"Main-Class" "aoc.runner"}}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :ns-compile ['aoc.runner]})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'aoc.runner}))
