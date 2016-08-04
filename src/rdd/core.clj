(ns rdd.core
  (:require [hickory.core :refer [parse as-hickory]]
            [hickory.select :as hs]))

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

(defn chiang-mai-peeps [raw-data]
  (->> raw-data
       (rest)
       (map prettify-raw)
       (filter (fn [{:keys [location]}] (re-seq #"Chiang" location)))))

(defn fetch-and-parse []
  (->> (slurp "https://gist.github.com/statguy/6489c06f2425c8836f2243ff01542c6b")
       (parse)
       (as-hickory)
       (#(hs/select (hs/tag :tr) %))))

(comment (chiang-mai-peeps (fetch-and-parse)))
