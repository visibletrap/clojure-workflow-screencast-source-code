(ns rdd.client.core
  (:require [reagent.core :as r]))

(defn root []
  [:h1 "Hello World"])

(defn ^:export start []
  #_(r/render-component [root] (.getElementById js/document "app")))

(defn on-js-reload []
  (start))
