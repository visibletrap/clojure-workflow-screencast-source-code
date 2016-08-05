(ns rdd.core
  (:require [hickory.core :refer [parse as-hickory]]
            [hickory.select :as hs]))

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

(defn chiang-mai-peeps [html-string]
  (->> html-string
       (parse)
       (as-hickory)
       (#(hs/select (hs/tag :tr) %))
       (rest)
       (map hickory->map)
       (filter (fn [{:keys [location]}] (re-seq #"Chiang" location)))))

(defn fetch []
  (slurp "https://gist.github.com/statguy/6489c06f2425c8836f2243ff01542c6b"))

(comment (chiang-mai-peeps (fetch)))
