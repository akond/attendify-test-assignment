(defproject attendify-test-assignment "0.1.0-SNAPSHOT"
	:description "Attendify test assignment"
	:url "https://github.com/akond/attendify-test-assignment"
	:license {:name "Eclipse Public License"
			  :url  "http://www.eclipse.org/legal/epl-v10.html"}

	:dependencies [[org.clojure/clojure "1.8.0"]
				   [org.clojure/core.async "0.3.441"]
				   [com.cemerick/url "0.1.1"]
				   [instaparse "1.4.5"]
				   [http-kit "2.2.0"]
				   [cheshire "5.7.0"]
				   [manifold "0.1.6"]]

	:main attendify-test-assignment.core)
