(ns attendify-test-assignment.core
	(:require
		[attendify-test-assignment.dribble-manifold :refer :all]
		[attendify-test-assignment.transport :as transport]
		[attendify-test-assignment.url-pattern :as url-pattern]
		[clojure.test :as test]
		))


(defn Task1 []
	(println "Task 1:")
	(let [access-token "8f998845c37e52ead2c3c2fa47e87951214d147aa0dc2aced4e6a0595e172bc8"]
		(time (pr (top-ten-likers (partial fetch-dribbble access-token) 1232)))
		(shutdown-agents)))


(defn Task2 []
	(println "Task 2:")
	(let [Dribbble2 (url-pattern/new-pattern "host(dribbble.com); path(shots/?id); queryparam(offset=?offset); queryparam(list=?type);")
		  result (url-pattern/recognize Dribbble2 "http://dribbble.com/shots/bradfitz?list=abc&offset=123")]
		(test/is (= [[:id "bradfitz"] [:offset "123"] [:list "abc"]] result
					))
		(clojure.pprint/pprint result)
		))

(defn -main []
	(println "Greetings!")

	(Task1)
	(Task2)

	(println "Done"))
