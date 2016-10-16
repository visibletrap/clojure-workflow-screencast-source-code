(ns rdd.client.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

(defonce S (r/atom {:search ""
                    :records []
                    :saving false}))

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
  (let [{:keys [search records saving]} @S
        filtered-records (filter (match-record search) records)]
    [:div
     [:input {:type :text
              :value search
              :placeholder "Type here"
              :on-change (fn [e] (swap! S assoc :search (.. e -target -value)))}]
     [:input {:type :button
              :value (if saving "Saving" "Save")
              :on-click (fn [_]
                          (swap! S assoc :saving true)
                          (POST
                            "/save"
                            {:format :json
                             :params {:search search}
                             :handler (fn [_] (swap! S assoc :saving false))
                             :error-handler #()}))
              :disabled saving}]
     [label search]
     [records-table filtered-records]]))

(defn mount-root []
  (r/render-component [root] (.getElementById js/document "app")))

(defn on-js-reload []
  (mount-root))

(defn ^:export start []
  (GET "/api" {:handler (fn [{:keys [search peeps]}]
                          (swap! S assoc :records peeps :search search))
               :response-format :json
               :keywords? true})
  (mount-root))
