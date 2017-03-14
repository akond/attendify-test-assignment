(ns attendify-test-assignment.url-pattern-test
	(:require [clojure.test :refer :all]
			  [attendify-test-assignment.url-pattern :as url-pattern]))


(deftest Pattern-Matching
	(testing "Simple path pattern"
		(let [SimplePathPattern (url-pattern/new-pattern "host(twitter.com); path(user/status/id);")]
			(is (true? (url-pattern/recognize SimplePathPattern "https://twitter.com/user/status/id")))
			(is (nil? (url-pattern/recognize SimplePathPattern "https://twitter.com/user/status/id2"))))
		)

	(testing "Twitter"
		(let [Twitter (url-pattern/new-pattern "host(twitter.com); path(?user/status/?id);")]
			(is (= [[:user "bradfitz"] [:id "562360748727611392"]]
				   (url-pattern/recognize Twitter "http://twitter.com/bradfitz/status/562360748727611392")))))

	(testing "Dribbble"
		(let [Dribbble (url-pattern/new-pattern "host(dribbble.com); path(shots/?id); queryparam(offset=?offset);")]
			(testing "negatives"
				(is (nil? (url-pattern/recognize Dribbble "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")))
				(is (nil? (url-pattern/recognize Dribbble "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users")))
				)

			(testing "positives"
				(is (= [[:id "1905065-Travel-Icons-pack"] [:offset "1"]]
					   (url-pattern/recognize Dribbble "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")))
				))
		)

	(testing "Dribbble 2"
		(let [Dribbble2 (url-pattern/new-pattern "host(dribbble.com); path(shots/?id); queryparam(offset=?offset); queryparam(list=?type);")]
			(is (= [[:id "bradfitz"] [:offset "123"] [:list "abc"]]
				   (url-pattern/recognize Dribbble2 "http://dribbble.com/shots/bradfitz?list=abc&offset=123"))))
		)
	)
