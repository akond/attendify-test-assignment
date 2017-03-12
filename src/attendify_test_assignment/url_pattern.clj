(ns attendify-test-assignment.url-pattern
	(:require [instaparse.core :as instaparse]
			  [cemerick.url :as url]))


(defprotocol UrlRecognizer
	(recognize [this s]))


(defrecord Pattern [schema]
	UrlRecognizer
	(recognize [this s]
		(letfn [(parse-path [p path]
					(let [re (re-pattern (str (apply str (map #(if (keyword? %) "(.+?)" %) p)) "$"))
						  keywords (filter keyword? p)]

						(if-let [matches (re-find re path)]
							(mapv vec (partition 2 (interleave keywords (next matches))))
							(throw (Exception. "Path does not match")))))


				(parse-single-parameter [url [k & [v :as params]]]
					(condp = k
						:host (when-not (= v (get url k))
								  (throw (Exception. "Host mismatch")))

						:path (let [adjusted-path (subs (get url k) 1)]
								  (if (= 1 (count params))
									  (when-not (= v adjusted-path) (throw (Exception. "Path mismatch")))
									  (parse-path params adjusted-path)))

						:queryparam (let [adjusted-param (subs v 0 (dec (count v)))
										  param-val (get-in url [:query adjusted-param])]
										(if (nil? param-val)
											(throw (Exception. (str "Missing " adjusted-param " parameter")))
											[[(keyword adjusted-param) param-val]]))
						params))

				(apply-schema [u]
					(reduce (partial apply conj)
							[]
							(filterv some? (mapv (partial parse-single-parameter u) schema))))]

			(try
				(let [result (apply-schema (url/url s))]
					(if (empty? result) true result))
				(catch Exception e
					;(prn (.getMessage e))
					nil)))
		))

(defn new-pattern [schema]
	(letfn [(parse-url-rule [s]
				(let [parser (instaparse/parser
								 "STATEMENTS = STATEMENT*
								  STATEMENT = NAME <'('> CONTENT + <')'> <';'> <SPACE?>
								  NAME = #\"\\w+\"
								  GROUP = <'?'> NAME
								  TEXT = DATA +
								  DATA = #\"[^\\(\\)\\?]\"
								  CONTENT = (GROUP / TEXT)
								  SPACE = #\"\\s*\"
								  ")]
					(instaparse/transform
						{:DATA       str
						 :TEXT       (comp identity str)
						 :GROUP      identity
						 :STATEMENTS vector
						 :STATEMENT  vector
						 :CONTENT    identity
						 :NAME       keyword}
						(parser s))))]

		(->Pattern (parse-url-rule schema)))
	)