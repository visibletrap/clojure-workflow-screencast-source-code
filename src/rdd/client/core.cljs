(ns rdd.client.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET]]))

(enable-console-print!)

(defonce S (r/atom {:search ""
                    :records []}))

(defn label [text]
  [:h1 text])

(def table-header
  [:rank :username :contribs :public-repos :public-gits
   :location :picture :hireable])

(defn table-row [row-data]
  [:tr
   (for [[i c] (map-indexed vector row-data)]
     ^{:key i} [:td c])])

(defn records-table [records]
  [:table
   [:thead
    [:tr
     (for [h table-header]
       ^{:key h} [:th h])]]
   [:tbody
    (for [r records
          :let [rl (map r table-header)]]
      ^{:key (:rank r)} [table-row rl])]])

(defn match-record [search]
  (fn [r] (re-find (re-pattern search) (:username r))))

(defn root []
  (let [{:keys [search records]} @S
        filtered-records (filter (match-record search) records)]
    [:div
     [:input {:type :text
              :value search
              :placeholder "Type here"
              :on-change (fn [e] (swap! S assoc :search (.. e -target -value)))}]
     [label search]
     [records-table filtered-records]]))

(defn mount-root []
  (r/render-component [root] (.getElementById js/document "app")))

(defn on-js-reload []
  (mount-root))

(defn ^:export start []
  (GET "/api" {:handler (fn [resp] (swap! S assoc :records resp))
               :response-format :json
               :keywords? true})
  (mount-root))
