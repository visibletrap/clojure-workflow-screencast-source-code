(ns rdd.core
  (:require [hickory.core :refer [parse as-hickory]]
            [hickory.select :as hs]
            [aleph.http :as aleph]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]))

(defn prettify-raw [record]
  (->> record
       :content
       (filter coll?)
       (map :content)
       (map first)
       ((fn [[rank {:keys [content]} contrib public-repos
              public-gits location {{:keys [href]} :attrs} hireable & _]]
          [rank (first content) contrib public-repos public-gits location href hireable]))
       (zipmap [:rank :username :contribs :public-repos :public-gits :location :picture :hireable])))

(defn peeps [raw-data]
  (->> raw-data
       (rest)
       (map prettify-raw)))

(defn fetch-and-parse []
  (->> (slurp "https://gist.github.com/statguy/6489c06f2425c8836f2243ff01542c6b")
       (parse)
       (as-hickory)
       (#(hs/select (hs/tag :tr) %))))

(defonce D (atom nil))

(defn init-data []
  (reset! D (peeps (fetch-and-parse))))

(def handler
  (wrap-json-response
    (fn [{:keys [uri]}]
      (case (#{"/" "/api"} uri)
        "/api" (response @D)
        "/" (response "<html><body><h1>HELLO</h1></body></html>")
        (response "nothing")))))

(defonce S (atom nil))

(defn start! []
  (reset! S (aleph/start-server handler {:port 8000})))

(defn stop! []
  (when-let [s @S] (.close s))
  (reset! S nil))

(defn restart! []
  (stop!)
  (start!))
