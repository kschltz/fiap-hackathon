(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.tools.deps :as t]))


(def version "1.0.0")
(def class-dir "target/classes")
(defn uber-file [project-name]
  (format "target/%s-%s-standalone.jar" project-name version))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [{:keys [project]}]
  (clean nil)

  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis      @basis
                  :ns-compile '[core]
                  :class-dir  class-dir})
  (b/uber {:class-dir class-dir
           :uber-file (uber-file project)
           :basis     @basis
           :main      'core}))


(defn test
      "Run all the tests."
      [opts]
      (println "\nRunning tests...")
      (let [basis (b/create-basis {:aliases [:test]})
            combined (t/combine-aliases basis [:test])
            cmds (b/java-command
                   {:basis     basis
                    :java-opts (:jvm-opts combined)
                    :main      'clojure.main
                    :main-args ["-m"
                                "cloverage.coverage"
                                "--codecov"
                                "--lcov"
                                "--no-html"
                                "--test-ns-path" "test"
                                "--src-ns-path" "src"]})
            {:keys [exit]} (b/process cmds)]
           (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
      opts)
