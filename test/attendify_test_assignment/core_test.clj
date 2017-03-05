(ns attendify-test-assignment.core-test
	(:require [clojure.test :refer :all]
			  [attendify-test-assignment.core :refer :all]
			  [cemerick.url :as url]
			  [clojure.pprint :refer :all]
			  [instaparse.core :as insta]))


(defrecord Pattern [schema])

(def SimplePathPattern (Pattern. "host(twitter.com); path(user/status/id);"))

(def twitter (Pattern. "host(twitter.com); path(?user/status/?id);"))

(def dribbble (Pattern. "host(dribbble.com); path(shots/?id); queryparam(offset=?offset);"))

(def dribbble2 (Pattern. "host(dribbble.com); path(shots/?id); queryparam(offset=?offset); queryparam(list=?type);"))


(defn- parse-url-rule [s]
	(let [parser (insta/parser
					 "STATEMENTS = STATEMENT*
					  STATEMENT = NAME <'('> CONTENT + <')'> <';'> <SPACE?>
					  NAME = #\"\\w+\"
					  GROUP = <'?'> NAME
					  TEXT = DATA +
					  DATA = #\"[^\\(\\)\\?]\"
					  CONTENT = (GROUP / TEXT)
					  SPACE = #\"\\s*\"
					  ")]
		(insta/transform
			{:DATA       str
			 :TEXT       (comp identity str)
			 :GROUP      identity
			 :STATEMENTS vector
			 :STATEMENT  vector
			 :CONTENT    identity
			 :NAME       keyword}
			(parser s))))


(defn- parse-path [p path]
	(let [re (re-pattern (str (apply str (map #(if (keyword? %) "(.+?)" %) p)) "$"))
		  keywords (filter keyword? p)]

		(if-let [matches (re-find re path)]
			(vec (map vec (partition 2 (interleave keywords (next matches)))))
			(throw (Exception. "Path does not match")))))


(defn- parse-single-parameter [url k & [v :as params]]
	(condp = k
		:host (if-not (= v (get url k)) (throw (Exception. "Host mismatch")))

		:path (let [adjusted-path (subs (get url k) 1)]
				  (if (= 1 (count params))
					  (if-not (= v adjusted-path) (throw (Exception. "Path mismatch")))
					  (parse-path params adjusted-path)))

		:queryparam (let [adjusted-param (subs v 0 (dec (count v)))
						  param-val (get-in url [:query adjusted-param])]
						(if (nil? param-val)
							(throw (Exception. (str "Missing " adjusted-param " parameter")))
							[[(keyword adjusted-param) param-val]]))
		params)
	)


(defn- parse-schema [t u]
	(vec (remove nil?
				 (loop [r []
						data t]
					 (if (empty? data)
						 r
						 (recur
							 (apply conj r (apply parse-single-parameter u (first data)))
							 (next data)))
					 ))))

(defn recognize [pattern s]
	(let [parsed-url (url/url s)
		  tree (parse-url-rule (:schema pattern))]
		(try
			(let [result (parse-schema tree parsed-url)]
				(if (empty? result) true result))
			(catch Exception e
				nil))))


(deftest task2
	(testing "Pattern creation"
		(is (true? (recognize SimplePathPattern "https://twitter.com/user/status/id")))
		(is (nil? (recognize SimplePathPattern "https://twitter.com/user/status/id2")))

		(is (nil? (recognize dribbble "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")))
		(is (nil? (recognize dribbble "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users")))

		(is (= [[:id "1905065-Travel-Icons-pack"] [:offset "1"]]
			   (recognize dribbble "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")))

		(is (= [[:user "bradfitz"] [:id "562360748727611392"]]
			   (recognize twitter "http://twitter.com/bradfitz/status/562360748727611392")))

		(is (= [[:id "bradfitz"] [:offset "123"] [:list "abc"]]
			   (recognize dribbble2 "http://dribbble.com/shots/bradfitz?list=abc&offset=123")))
		))
