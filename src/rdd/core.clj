(ns rdd.core
  (:require [hickory.core :refer [parse as-hickory]]
            [hickory.select :as hs]
            [aleph.http :as aleph]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [hiccup.page :refer [html5]]
            [hiccup.form :refer [form-to]]))

(defn extract-nested-value [record-list]
  (-> record-list
      (vec)
      (update 1 (fn [{[username] :content}] username))
      (update 6 (fn [{{:keys [href]} :attrs}] href))))

(defn hickory->map [record]
  (->> record
       :content
       (filter coll?)
       (map :content)
       (map first)
       (extract-nested-value)
       (zipmap [:rank :username :contribs :public-repos :public-gits :location :picture :hireable])))

(defn peeps [html-string]
  (->> html-string
       (parse)
       (as-hickory)
       (#(hs/select (hs/tag :tr) %))
       (rest)
       (map hickory->map)))

(defn fetch []
  (slurp "https://gist.github.com/statguy/6489c06f2425c8836f2243ff01542c6b"))

;; Domain model

(defonce D (atom nil))

(defn init-data []
  (reset! D (peeps (fetch))))

;; Web layer

(defn page []
  (html5
    [:body
     [:div#app "Blank"]
     [:script {:src "/js/main.js"}]
     [:script "rdd.client.core.start();"]]))

(defroutes approutes
  (GET "/" [] (page))
  (GET "/api" [] (response @D))
  (GET "/favicon.ico" [] ""))

(def handler
  (-> approutes
      (wrap-json-response)
      (wrap-resource "public")))

;; Web server

(defonce S (atom nil))

(defn start-server! []
  (init-data)
  (reset! S (aleph/start-server handler {:port 8000})))

(defn stop-server! []
  (when-let [s @S] (.close s))
  (reset! S nil))

(defn restart-server! []
  (stop-server!)
  (start-server!))

;; System

(defn restart! []
  (init-data)
  (restart-server!))

(defn -main []
  (restart!))
