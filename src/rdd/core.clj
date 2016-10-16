(ns rdd.core
  (:require [hickory.core :refer [parse as-hickory]]
            [hickory.select :as hs]
            [aleph.http :as aleph]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET POST]]
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
  (reset! D {:peeps (peeps (fetch))
             :search ""}))

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
  (POST "/save" [search]
    (swap! D assoc :search search)
    (response "Saved"))
  (GET "/favicon.ico" [] ""))

(def handler
  (-> approutes
      (wrap-keyword-params)
      (wrap-json-params)
      (wrap-json-response)
      (wrap-resource "public")))

;; Web server

(defonce S (atom nil))

(defn start-server! []
  (reset! S (aleph/start-server #'handler {:port 8000})))

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

(comment
  (restart-server!)
  (restart!))
