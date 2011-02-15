#!/usr/bin/python
import tornado.httpserver
import tornado.ioloop
import tornado.web

import optparse

from handler import MainHandler

#Default options
WEBROOT = "webroot"
PORT = 8042

if __name__=="__main__":
	parser = optparse.OptionParser()
	parser.add_option('--webroot','-w',default=WEBROOT)
	parser.add_option('--port','-p',default=PORT,type=int)
	opts = parser.parse_args()[0]
	MainHandler.WEBROOT = opts.webroot
	print "Running DBox server on port %i with a web root of '%s' (see `./server.py -h` for options)." % (opts.port,opts.webroot)

	#Handle all urls using MainHandler, with the text following the slash being one parameter
	application = tornado.web.Application([ ("/(.*)",MainHandler) ])
	#Start up the server on port 8042 and begin an IO loop with it
	server = tornado.httpserver.HTTPServer(application)
	server.listen(PORT)
	tornado.ioloop.IOLoop.instance().start()
