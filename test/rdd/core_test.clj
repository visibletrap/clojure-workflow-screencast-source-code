(ns rdd.core-test
  (:require [clojure.test :refer :all]
            [rdd.core :refer [chiang-mai-peeps]]))

(deftest chiang-mai-peeps-test
  (let [input (str "<html><body><table><tr></tr><tr>"
                   "<td>#388</td>"
                   "not td"
                   "<td><a>mestizo</a></td>"
                   "<td>2</td>"
                   "<td>7</td>"
                   "<td>3</td>"
                   "<td>Chiang Mai, Thailand</td>"
                   "<td><a href=\"https://avatars1.githubusercontent.com/u/1292803?v=3&s=400\"></a></td>"
                   "<td>Yes</td>"
                   "</table></tr></body></html>")
        output [{:rank "#388",
                 :username "mestizo",
                 :contribs "2",
                 :public-repos "7",
                 :public-gits "3",
                 :location "Chiang Mai, Thailand",
                 :picture "https://avatars1.githubusercontent.com/u/1292803?v=3&s=400",
                 :hireable "Yes"}]]
    (is (= (chiang-mai-peeps input) output))))
