(ns attendify-test-assignment.transport-test
	(:require [clojure.test :refer :all]
			  [attendify-test-assignment.transport :refer :all]))


(deftest Transport
	(testing "Fully qualified name"
		(let [t (create-transport "http://sample.com" nil nil)]
			(is (= "http://sample.com/haha" (fqn t "/haha")))
			(is (= "http://sample.com/user/523" (fqn t "user/523")))
			(is (= "http://sample.com/ignore" (fqn t "http://day.com/ignore")))
			(is (= "http://sample.com/ignore?p=2" (fqn t "http://day.com/ignore?p=2")))
			)))
