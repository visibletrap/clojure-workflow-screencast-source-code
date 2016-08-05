(ns rdd.core
  (:require [hickory.core :refer [parse as-hickory]]
            [hickory.select :as hs]
            [aleph.http :as aleph]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]))

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


(defonce D (atom nil))

(defn init-data []
  (reset! D (peeps (fetch))))

(def handler
  (wrap-json-response
    (fn [{:keys [uri]}]
      (case (#{"/" "/api"} uri)
        "/api" (response @D)
        "/" (response "<html><body><h1>HELLO</h1></body></html>")
        (response "nothing")))))

(defonce S (atom nil))

(defn start! []
  (init-data)
  (reset! S (aleph/start-server handler {:port 8000})))

(defn stop! []
  (when-let [s @S] (.close s))
  (reset! S nil))

(defn restart! []
  (stop!)
  (start!))
