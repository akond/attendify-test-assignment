(defproject attendify-test-assignment "0.1.0-SNAPSHOT"
	:description "FIXME: write description"
	:url "http://example.com/FIXME"
	:license {:name "Eclipse Public License"
			  :url  "http://www.eclipse.org/legal/epl-v10.html"}
	:dependencies [[org.clojure/clojure "1.8.0"]
				   [org.clojure/core.async "0.3.441"]
				   [clj-http "2.3.0"]
				   [org.clojure/data.json "0.2.6"]
				   [com.cemerick/url "0.1.1"]
				   [instaparse "1.4.5"]]
	:plugins [[lein-ring "0.9.7"]]
	:ring {:handler attendify-test-assignment.core/handler}
	:main attendify-test-assignment.core)
