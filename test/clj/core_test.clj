(ns core-test
  (:require
   [clojure.test :refer :all]
   [ring.mock.request :refer :all]
   [rss-history.handler :refer :all]
   [rss-history.rss :refer :all]
   [clojure.java.io :as io]
   [clj-rss.core :as rss]
   [feedparser-clj.core :as feedparser]))

(declare fixture-feed)

(deftest can-get-atom-feed-from-internet
  (let [num-entries 5
        feed (feedparser/parse-feed (str "http://anonymousmugwump.blogspot.co.uk/feeds/posts/default?max-results=" num-entries))]
    (is (= num-entries (count (:entries feed))))))

;; terry tao
;; johncarlosbaez
;; ncatlab

(deftest can-take-atom-feed-and-reproduce
  )

#_(deftest can-produce-rss-feed-with-fixture-data-from-rss-namespace
  (is (= fixture-feed 
         (->> deduplicated
              rename-rss-keys
              dissoc-rss-keys
              rejigger-description-blogspot
              (take 5)
              (rss/channel-xml {:title "foobar"
                                :link "link"
                                :description "descriptionfoobar"})))))

(def fixture-feed
"<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0' xmlns:atom='http://www.w3.org/2005/Atom'>\n<channel>\n<atom:link href='link' rel='self' type='application/rss+xml'/>\n<title>\nfoobar\n</title>\n<link>\nlink\n</link>\n<description>\ndescriptionfoobar\n</description>\n<generator>\nclj-rss\n</generator>\n<item>\n<description>\nThere is a fascinating set of ideas that has been swirling around in the global zeitgeist for the past decade, around the quote that will keep Donald Rumsfeld in the history books long after his political career is forgotten. I am referring, of course, to the famous unknown-unknowns quote from 2002. Here it is: [T]here are [...]\n</description>\n<pubDate>\nThu, 22 Mar 2012 16:38:18 -0400\n</pubDate>\n<title>\nCan Hydras Eat Unknown-Unknowns for Lunch?\n</title>\n<link>\nhttp://feeds.ribbonfarm.com/~r/Ribbonfarm/~3/Vrs1qjMuEyc/\n</link>\n<guid>\nhttp://www.ribbonfarm.com/?p=3170\n</guid>\n</item>\n<item>\n<description>\nPondering the A. G. Lafley HBR piece that&amp;#8217;s been doing the rounds lately, I think I&amp;#8217;ve finally really figured out the difference between managers, leaders and workers. The title, and this cartoon I made up, capture the essence of my argument: all three archetypes within the world of business are defined by how they self-destruct. This [...]\n</description>\n<pubDate>\nTue, 23 Jun 2009 00:35:53 -0400\n</pubDate>\n<title>\nNeurotic Leaders, Paternalistic Managers and Self-Absorbed Workers\n</title>\n<link>\nhttp://feeds.ribbonfarm.com/~r/Ribbonfarm/~3/HuPX5MiuBQ0/\n</link>\n<guid>\nhttp://www.ribbonfarm.com/?p=1057\n</guid>\n</item>\n<item>\n<description>\nFor my fourth video blog, I bring you a wide-ranging conversation with David Bosshart, CEO of the Gottlieb Duttweiler Institute (GDI) in Zurich. I&amp;#8217;ve known the folks at GDI for a few years, and worked with them several times. Most recently, GDI undertook the German translation of my Breaking Smart essays. This conversation is partly me interviewing [&amp;#8230;]\n</description>\n<pubDate>\nTue, 18 Oct 2016 14:29:16 -0400\n</pubDate>\n<title>\nCan the European Union Break Smart?\n</title>\n<link>\nhttp://feeds.ribbonfarm.com/~r/Ribbonfarm/~3/3YvtmrD9Bp4/\n</link>\n<guid>\nhttp://www.ribbonfarm.com/?p=5642\n</guid>\n</item>\n<item>\n<description>\nThis is a guest post by Craig Roche, a data scientist and artisanal landlord. Whiskey is very easy to make.  Farmers used to make it at home using their crops, and Henry Ford designed the Model T to run on home-distilled ethanol.  George Washington distilled 55,000 bottles/year when he retired from being President. Even the mutineers [&amp;#8230;]\n</description>\n<pubDate>\nTue, 30 Sep 2014 13:26:18 -0400\n</pubDate>\n<title>\nThe Political Hangover of Prohibition\n</title>\n<link>\nhttp://feeds.ribbonfarm.com/~r/Ribbonfarm/~3/gONM7UjTWLA/\n</link>\n<guid>\nhttp://www.ribbonfarm.com/?p=4761\n</guid>\n</item>\n<item>\n<description>\nKevin is a 2013 blogging resident visiting us from his home blog over at Melting Asphalt. To scandalize a member of the educated West, open any book on European table manners from the middle of the second millennium: &amp;#8220;Some people gnaw a bone and then put it back in the dish. This is a serious offense.&amp;#8221; — Tannhäuser, 13th century. &amp;#8220;Don&amp;#8217;t [&amp;#8230;]\n</description>\n<pubDate>\nThu, 07 Nov 2013 13:41:09 -0500\n</pubDate>\n<title>\nUX and the Civilizing Process\n</title>\n<link>\nhttp://feeds.ribbonfarm.com/~r/Ribbonfarm/~3/rdktYk_JK9E/\n</link>\n<guid>\nhttp://www.ribbonfarm.com/?p=4388\n</guid>\n</item>\n</channel>\n</rss>\n")
#_(->> (:entries blah)
     rejigger-description-blogspot
     (map escape-description-in-feed)
     rename-rss-keys
     dissoc-rss-keys
     (rss/channel-xml {:title "foobar"
                       :link "link"
                       :description "descriptionfoobar"})
     (spit "withescaping.xml")
     )


#_(deftest invalid-xml)
#_ (let [r 182
         baseurl  "http://www.thesartorialist.com/feed/?paged="]
     (reset! entries [])
     (doall 
      (for [n r]
        (let [_         (print n)
              all-feeds (:entries (feedparser/parse-feed (str baseurl n)))]
          (doall
           (for [feed all-feeds]
             (swap! entries conj feed))))))
     'dope
     )


#_ (let [r (range 1 182)
         baseurl  "http://www.thesartorialist.com/feed/?paged="]
     (reset! entries [])
     (doall 
      (for [n r]
        (let [_         (print n)
              all-feeds (:entries (feedparser/parse-feed (str baseurl n)))]
          (doall
           (for [feed all-feeds]
             (swap! entries conj feed))))))
     'dope
     )
