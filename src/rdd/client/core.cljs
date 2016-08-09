(ns rdd.client.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(defonce S (r/atom ""))

(defn label [text]
  [:h1 text])

(defn root []
  (let [text @S]
    [:div
     [:input {:type :text
              :value text
              :placeholder "Type here"
              :on-change (fn [e] (reset! S (.. e -target -value)))}]
     [label text]
     [label "FIX TEXT"]]))

(defn mount-root []
  (r/render-component [root] (.getElementById js/document "app")))

(defn on-js-reload []
  (mount-root))

(defn ^:export start []
  ; Call api through ajax and store retrived data
  (mount-root))
