(set-env!
 :source-paths    #{"src"}
 :resource-paths  #{"resources"}
 :dependencies '[[org.clojure/clojurescript "1.7.107"]
                 [adzerk/boot-cljs          "1.7.48-3"        :scope "test"]
                 [adzerk/boot-cljs-repl     "0.1.10-SNAPSHOT" :scope "test"]
                 [adzerk/boot-reload        "0.3.2-SNAPSHOT"  :scope "test"]
                 [weasel                    "0.7.0"           :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.12"          :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"           :scope "test"]
                 [cljsjs/fabric             "1.5.0-0"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]])

(deftask build []
  (comp (cljs :ids #{"main"}
              :optimizations :simple)
        (cljs :ids #{"renderer"}
              :optimizations :advanced)))

(deftask dev []
  (comp 
        (speak)
        (watch)
;;        (serve)
        (cljs-repl :ids #{"renderer"})
        (reload    :ids #{"renderer"}
                   :on-jsload 'app.renderer/init)
        
        (cljs      :ids #{"renderer"})

        (cljs      :ids #{"main"}
                   :compiler-options {:asset-path "target/main.out"
                                      :closure-defines {'app.main/dev? true}
                                      :parallel-build true})))

(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.11.0-SNAPSHOT"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)
